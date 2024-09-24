package decimal.apigateway.service.security;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApiAuthorizationConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.ServiceDef;
import decimal.apigateway.service.ApplicationDefConfig;
import decimal.apigateway.service.validator.ValidatorFactory;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static decimal.apigateway.enums.RequestValidationTypes.*;


@Log
@Service
public class ValidationServiceV2Impl implements ValidationServiceV2 {

    private final ValidatorFactory validatorFactory;

    @Autowired
    ApplicationDefConfig applicationDefConfig;

    @Autowired
    public ValidationServiceV2Impl(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }


    @Override
    public ResponseEntity validateExecutionRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        sourceAppValidator(request, httpHeaders);

        destinationAppValidator(request, httpHeaders);

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(Constant.SUCCESS_STATUS);
        response.setMessage("Validation for executing a request has been done successfully");

        MicroserviceResponse validationResponse = validatorFactory.getValidator(SERVICE_NAME.name()).validate(request, httpHeaders);

        List<ServiceDef> logs = (List<ServiceDef>) validationResponse.getResponse();

        Map<String, String> customData = new HashMap<>();
        customData.put("appLogs", applicationDefConfig.getIsLogsRequiredFlag(httpHeaders));

        String keysToMask = logs.stream().filter(serviceDef -> serviceDef.getKeysToMask() != null && !serviceDef.getKeysToMask().isEmpty()).map(ServiceDef::getKeysToMask).collect(Collectors.joining(","));

        List<String> logsEnabledList = logs.stream().map(ServiceDef::getIsAuditEnabled).collect(Collectors.toList());

        customData.put("serviceLog", logsEnabledList.contains("Y") ? "Y" : "N");
        customData.put(Constant.KEYS_TO_MASK, keysToMask);
        customData.put("logpurgedays", logs.get(0).getLogPurgeDays());
        customData.put("destinationOrgId", httpHeaders.get(Headers.destinationorgid.name()));
        response.setCustomData(customData);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);
    }

    private void sourceAppValidator(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("httpHeaders---------------          "+httpHeaders);
        String sourceOrgId = httpHeaders.get(Headers.sourceorgid.name());
        String sourceAppId = httpHeaders.get(Headers.sourceappid.name());
        log.info("sourceOrgId----------"+sourceOrgId+"sourceAppId========"+sourceAppId);

        httpHeaders.put(Headers.orgid.name(), sourceOrgId);
        httpHeaders.put(Headers.appid.name(), sourceAppId);

        log.info("httpHeaders----------"+httpHeaders);

        log.info("validating the source app with orgId: " + sourceOrgId + " and appId: " + sourceAppId);

        RequestValidationTypes[] requestValidationTypesArr = {APPLICATION, INACTIVE_SESSION, SESSION, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            log.info(requestValidationTypes + " validation in-progress for source app with appId: " + sourceAppId);
            validatorFactory.getValidator(requestValidationTypes.name()).validate(request, httpHeaders);
        }
    }

    private void destinationAppValidator(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        MicroserviceResponse accessMgmtResponse = validatorFactory.getValidator(API_AUTHORIZATION.name()).validate(request, httpHeaders);

        ApiAuthorizationConfig apiAuthorizationConfig = (ApiAuthorizationConfig) accessMgmtResponse.getResponse();

        String destinationOrgId = apiAuthorizationConfig.getDestinationOrgId();
        String destinationAppId = apiAuthorizationConfig.getDestinationAppId();

        log.info("destination app id -- " + destinationAppId);
        httpHeaders.put(Headers.destinationorgid.name(), destinationOrgId);

        httpHeaders.put(Headers.orgid.name(), destinationOrgId);
        httpHeaders.put(Headers.appid.name(), destinationAppId);

        log.info("validating the destination app with orgId: " + destinationOrgId + " and appId: " + destinationAppId);

        RequestValidationTypes[] requestValidationTypesArr = {SERVICE_SCOPE, SERVICE_NAME, IP};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            log.info(requestValidationTypes + " validation in-progress for destination app with appId: " + destinationAppId);
            validatorFactory.getValidator(requestValidationTypes.name()).validate(request, httpHeaders);
        }
    }
}
