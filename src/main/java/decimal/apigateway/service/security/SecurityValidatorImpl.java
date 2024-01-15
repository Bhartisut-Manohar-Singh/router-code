package decimal.apigateway.service.security;

import decimal.apigateway.commons.Constants;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ApiAuthorizationConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.*;
import decimal.apigateway.service.ApplicationDefConfig;
import decimal.apigateway.service.validator.Validator;
import decimal.apigateway.service.validator.ValidatorFactory;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static decimal.apigateway.commons.Constant.IS_DIGITALLY_SIGNED;
import static decimal.apigateway.commons.Constant.IS_PAYLOAD_ENCRYPTED;
import static decimal.apigateway.enums.RequestValidationTypes.*;
import static decimal.apigateway.enums.RequestValidationTypes.API_AUTHORIZATION;


@Service
@Log
public class SecurityValidatorImpl implements SecurityValidator {

    @Autowired
    @Qualifier("txnKeyValidator")
    Validator txnKeyValidator;

    @Autowired
    @Qualifier("serviceValidator")
    Validator serviceValidator;

    @Autowired
    AuthSecurity authSecurity;

    @Autowired
    @Qualifier("authHeaderValidator")
    Validator authHeaderValidator;

    @Autowired
    @Qualifier("publicRequestValidator")
    Validator publicRequestValidator;

    @Autowired
    Request requestObj;

    @Autowired
    ValidatorFactory validatorFactory;

    @Override
    public MicroserviceResponse validateRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("+++++++++++++In SecurityValidatorImpl service ++++++++++++++++");
        MicroserviceResponse microserviceResponse = authHeaderValidator.validate(request, httpHeaders);

        Account account = (Account) microserviceResponse.getResponse();
        // Validate hash data and application client secret
        authSecurity.checkAuthDataHash(account.getSecurityVersion(), account, account.getAuthorization(), account.getRequestId());

        return new MicroserviceResponse(Constants.SUCCESS_STATUS, "Success", account);
    }

    @Autowired
    ApplicationDefConfig applicationDefConfig;
    
    @Override
    public MicroserviceResponse validateAuthenticationRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("Validating authentication request");
        MicroserviceResponse microserviceResponse = authHeaderValidator.validate(request, httpHeaders);

        Account account = (Account) microserviceResponse.getResponse();

        List<java.lang.String> userNameData = RouterOperations.getStringArray(account.getUsername(), Constants.TILD_SPLITTER);

        httpHeaders.put(Headers.orgid.name(), userNameData.get(0));
        httpHeaders.put(Headers.appid.name(), userNameData.get(1));

        MicroserviceResponse validateResponse = serviceValidator.validate(request, httpHeaders);

        List<ServiceDef> serviceDefs = (List<ServiceDef>) validateResponse.getResponse();

        String keysToMask = serviceDefs.stream().filter(serviceDef -> serviceDef.getKeysToMask() != null && !serviceDef.getKeysToMask().isEmpty()).map(ServiceDef::getKeysToMask).collect(Collectors.joining(","));

        Map<String, String> customData = new HashMap<>();
        customData.put(Constants.KEYS_TO_MASK, keysToMask);

        List<String> logsEnabledList = serviceDefs.stream().map(ServiceDef::getIsAuditEnabled).collect(Collectors.toList());

        customData.put("serviceLog", logsEnabledList.contains("Y") ? "Y" : "N");
        customData.put(Constants.KEYS_TO_MASK, keysToMask);
        customData.put("logpurgedays",serviceDefs.get(0).getLogPurgeDays());
        customData.put("appLogs", applicationDefConfig.getIsLogsRequiredFlag(httpHeaders));

        StringJoiner applicationUser = new StringJoiner(Constants.TILD_SPLITTER);
        applicationUser.add(userNameData.get(0));
        applicationUser.add(userNameData.get(1));
        applicationUser.add(userNameData.get(3));

        httpHeaders.put(Headers.username.name(), applicationUser.toString());

        txnKeyValidator.validate(request, httpHeaders);

        Object plainRequest = authSecurity.decryptRequestV1(RouterOperations.getJsonObject(request).get("request").getAsString(), httpHeaders);

        log.info("Request has been decrypted successfully");
        log.info("Validation for authentication request has been done successfully");

        return new MicroserviceResponse(Constants.SUCCESS_STATUS, account.getUsername(), plainRequest, customData);
    }

    @Override
    public MicroserviceResponse validateAuthenticationRequestV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("Validating authentication request");
        MicroserviceResponse microserviceResponse = authHeaderValidator.validate(request, httpHeaders);

        log.info("validateAuthenticationRequestV2: response returned by auth header validator - " + microserviceResponse);

        Account account = (Account) microserviceResponse.getResponse();

        List<java.lang.String> userNameData = RouterOperations.getStringArray(account.getUsername(), Constants.TILD_SPLITTER);

        httpHeaders.put(Headers.orgid.name(), userNameData.get(0));
        httpHeaders.put(Headers.appid.name(), userNameData.get(1));

        log.info("Current org Id - " + userNameData.get(0));
        log.info("Current app Id - " + userNameData.get(1));

        StringJoiner applicationUser = new StringJoiner(Constants.TILD_SPLITTER);
        applicationUser.add(userNameData.get(0));
        applicationUser.add(userNameData.get(1));
        applicationUser.add(userNameData.get(3));

        log.info("application user - " + applicationUser);

        httpHeaders.put(Headers.username.name(), applicationUser.toString());

        log.info("Going to validate txn key with updated headers - ");

        httpHeaders.forEach((k,v) -> log.info(k + " -> " + v));

        txnKeyValidator.validate(request, httpHeaders);

        log.info("Txn key validated successfully....");

        // source validation finished

        //now starting destination app validation, setting the org and app for destination
        httpHeaders.put(Headers.sourceappid.name(), httpHeaders.get(Headers.appid.name()));

        log.info("source app id - " + httpHeaders.get(Headers.sourceappid.name()));

        MicroserviceResponse accessMgmtResponse = validatorFactory.getValidator(API_AUTHORIZATION.name()).validate(request, httpHeaders);

        ApiAuthorizationConfig apiAuthorizationConfig = (ApiAuthorizationConfig) accessMgmtResponse.getResponse();

        String destinationOrgId = apiAuthorizationConfig.getDestinationOrgId();
        String destinationAppId = apiAuthorizationConfig.getDestinationAppId();

        log.info("destination app id -- " + destinationAppId);
        httpHeaders.put(Headers.destinationorgid.name(), destinationOrgId);

        httpHeaders.put(Headers.orgid.name(), destinationOrgId);
        httpHeaders.put(Headers.appid.name(), destinationAppId);

        log.info("Now validating service level for - " + httpHeaders.get(Headers.orgid.name()) + " ~ " + httpHeaders.get(Headers.appid.name()));

        MicroserviceResponse validateResponse = serviceValidator.validate(request, httpHeaders);

        List<ServiceDef> serviceDefs = (List<ServiceDef>) validateResponse.getResponse();

        String keysToMask = serviceDefs.stream().filter(serviceDef -> serviceDef.getKeysToMask() != null && !serviceDef.getKeysToMask().isEmpty()).map(ServiceDef::getKeysToMask).collect(Collectors.joining(","));

        Map<String, String> customData = new HashMap<>();
        customData.put(Constants.KEYS_TO_MASK, keysToMask);

        List<String> logsEnabledList = serviceDefs.stream().map(ServiceDef::getIsAuditEnabled).collect(Collectors.toList());

        customData.put("serviceLog", logsEnabledList.contains("Y") ? "Y" : "N");
        customData.put(Constants.KEYS_TO_MASK, keysToMask);
        customData.put("logpurgedays",serviceDefs.get(0).getLogPurgeDays());
        customData.put("appLogs", applicationDefConfig.getIsLogsRequiredFlag(httpHeaders));
        customData.put("destinationOrgId", destinationOrgId);

        //Again switching org and app headers back to source app
        httpHeaders.put(Headers.orgid.name(), userNameData.get(0));
        httpHeaders.put(Headers.appid.name(), userNameData.get(1));

        log.info("Decrypting Request with org " + httpHeaders.get(Headers.orgid.name()) + " and app " + httpHeaders.get(Headers.appid.name()));
        Object plainRequest = authSecurity.decryptRequestV1(RouterOperations.getJsonObject(request).get("request").getAsString(), httpHeaders);

        log.info("Request has been decrypted successfully");

        log.info("Validation for authentication request has been done successfully");

        return new MicroserviceResponse(Constants.SUCCESS_STATUS, account.getUsername(), plainRequest, customData);

    }

    private void destinationAppValidator(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        httpHeaders.put(Headers.sourceappid.name(), httpHeaders.get(Headers.appid.name()));

        log.info("source app id - " + httpHeaders.get(Headers.sourceappid.name()));

        MicroserviceResponse accessMgmtResponse = validatorFactory.getValidator(API_AUTHORIZATION.name()).validate(request, httpHeaders);

        ApiAuthorizationConfig apiAuthorizationConfig = (ApiAuthorizationConfig) accessMgmtResponse.getResponse();

        String destinationOrgId = apiAuthorizationConfig.getDestinationOrgId();
        String destinationAppId = apiAuthorizationConfig.getDestinationAppId();

        log.info("destination app id -- " + destinationAppId);
        httpHeaders.put(Headers.destinationorgid.name(), destinationOrgId);

        httpHeaders.put(Headers.orgid.name(), destinationOrgId);
        httpHeaders.put(Headers.appid.name(), destinationAppId);

        log.info("validating the destination app with orgId: " + destinationOrgId + " and appId: " + destinationAppId);

        RequestValidationTypes[] requestValidationTypesArr = {SERVICE_NAME};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            log.info(requestValidationTypes + " validation in-progress for " + requestValidationTypes.name() + " and destination app with appId: " + destinationAppId);
            validatorFactory.getValidator(requestValidationTypes.name()).validate(request, httpHeaders);
        }
    }

    @Override
    public MicroserviceResponse validatePublicRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        log.info("+++++++++++++In validatePublicRegistration service ++++++++++++++++");
        MicroserviceResponse microserviceResponse = publicRequestValidator.validate(request, httpHeaders);

        ApplicationDef applicationDef = (ApplicationDef) microserviceResponse.getResponse();

        log.info("Validating client secret...");

        String clientSecret = httpHeaders.get(Constants.CLIENT_SECRET);


        if(!applicationDef.getClientSecret().equals(clientSecret)){
            log.info("Invalid client secret : " + clientSecret);
            log.info("Actual client secret : " + applicationDef.getClientSecret());
            throw new RouterException(RouterResponseCode.INVALID_CLIENT_SECRET, (Exception) null, Constants.ROUTER_ERROR_TYPE_SECURITY, "Invalid Client Secret.");
        }

        return new MicroserviceResponse(Constants.SUCCESS_STATUS, "Success", applicationDef);

    }

    @Override
    public ResponseEntity validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put(Headers.servicename.name(), serviceName);

        MicroserviceResponse validationResponse = validatorFactory.getValidator(SERVICE_NAME.name()).validate(request, httpHeaders);

        List<ServiceDef> logs  = (List<ServiceDef>) validationResponse.getResponse();

        String keysToMask = logs.stream().filter(serviceDef -> serviceDef.getKeysToMask() != null && !serviceDef.getKeysToMask().isEmpty()).map(ServiceDef::getKeysToMask).collect(Collectors.joining(","));

        List<String> logsEnabledList = logs.stream().map(ServiceDef::getIsAuditEnabled).collect(Collectors.toList());
        List<String> isPayloadEncryptedList = logs.stream().map(ServiceDef::getIsPayloadEncrypted).collect(Collectors.toList());
        List<String> isDigitallySignedList = logs.stream().map(ServiceDef::getIsDigitallySigned).collect(Collectors.toList());


        Map<String, Object> map = new HashMap<>();
        map.put("logsrequired", applicationDefConfig.getIsLogsRequiredFlag(httpHeaders));
        map.put("serviceLog", logsEnabledList.contains("Y") ? "Y" : "N");
        map.put(IS_PAYLOAD_ENCRYPTED, isPayloadEncryptedList.contains("Y") ? "Y" : "N");
        map.put(IS_DIGITALLY_SIGNED, isDigitallySignedList.contains("Y") ? "Y" : "N");
        map.put("logpurgedays",logs.get(0).getLogPurgeDays());
        map.put(Constants.KEYS_TO_MASK, keysToMask);

        RequestValidationTypes[] requestValidationTypes = null;

        if(isPayloadEncryptedList.contains("Y")  || isDigitallySignedList.contains("Y") ) {
            requestValidationTypes = new RequestValidationTypes[]{HEADERS, IP, SERVICE_SCOPE};
            ApplicationDef applicationDef = applicationDefConfig.findByOrgIdAndAppId(httpHeaders.get(Headers.orgid.name()), httpHeaders.get(Headers.appid.name()));
            map.put(Headers.clientsecret.name(), applicationDef.getClientSecret());
        }

        else
            requestValidationTypes = new RequestValidationTypes[]{HEADERS, CLIENT_SECRET, IP, SERVICE_SCOPE};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            validatorFactory.getValidator(plainRequestValidation.name()).validate(request, httpHeaders);
        }

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage("Validation for executing a request has been done successfully");


        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("headers", map);

        response.setResponse(responseMap);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        return new ResponseEntity<>(response,responseHeaders, HttpStatus.OK);
    }

    @Override
    public MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        RequestValidationTypes[] requestValidationTypesArr = {APPLICATION, INACTIVE_SESSION, SESSION, IP, SERVICE_SCOPE, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            validatorFactory.getValidator(requestValidationTypes.name()).validate(request, httpHeaders);
        }

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage("Validation for executing a request has been done successfully");


        MicroserviceResponse validationResponse = validatorFactory.getValidator(SERVICE_NAME.name()).validate(request, httpHeaders);

        List<ServiceDef> logs  = (List<ServiceDef>) validationResponse.getResponse();

        Map<String, String> customData = new HashMap<>();
        customData.put("appLogs", applicationDefConfig.getIsLogsRequiredFlag(httpHeaders));

        String keysToMask = logs.stream().filter(serviceDef -> serviceDef.getKeysToMask() != null && !serviceDef.getKeysToMask().isEmpty()).map(ServiceDef::getKeysToMask).collect(Collectors.joining(","));

        List<String> logsEnabledList = logs.stream().map(ServiceDef::getIsAuditEnabled).collect(Collectors.toList());

        customData.put("serviceLog", logsEnabledList.contains("Y") ? "Y" : "N");
        customData.put(Constants.KEYS_TO_MASK, keysToMask);
        customData.put("logpurgedays",logs.get(0).getLogPurgeDays());

        response.setCustomData(customData);

        return response;
    }
}
