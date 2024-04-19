package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.SecurityService;
import decimal.logs.model.AuditPayload;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;

@Service
@Log
public class RequestValidatorV2 {

    private SecurityService securityService;

    @Autowired
    public RequestValidatorV2(SecurityService securityService) {
        this.securityService= securityService;
    }


    public Object validateRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        return securityService.validateRegistration(request, httpHeaders);
        //return securityClient.validateRegistration(request, httpHeaders).getResponse();
    }

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) throws RouterException, IOException {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put(Headers.sourceappid.name(), clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put(Headers.sourceorgid.name(), clientId.split(Constant.TILD_SPLITTER)[1]);

        log.info("V2: Finally calling security client"+httpHeaders);

        MicroserviceResponse response = securityService.validate(request, httpHeaders, REQUEST.name());
        //MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypesV1.REQUEST.name());

        log.info("V2: Finally response returned by security client");

        String userName = response.getResponse().toString();

        log.info("V2: username returned -- " + userName);

        int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);

        auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

        httpHeaders.forEach((k,v) -> log.info(k + "->" + v));




        response = securityService.validateExecutionRequestV2(request, httpHeaders);
        //response = securityClient.validateExecutionRequestV2(request, httpHeaders);

        Map<String, String> customData = response.getCustomData();
        httpHeaders.put("destinationorgid",customData.get("destinationOrgId"));
        httpHeaders.put("logsrequired", customData.get("appLogs"));
        httpHeaders.put("serviceLogs", customData.get("serviceLog"));
        httpHeaders.put(Constant.KEYS_TO_MASK, customData.get(Constant.KEYS_TO_MASK));
        httpHeaders.put("logpurgedays",customData.get("logpurgedays"));


        return httpHeaders;

    }

    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {
        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("sourceOrgId") + "~" +httpHeaders.get(Headers.sourceappid.name()));
        httpHeaders.put("username", httpHeaders.get("clientid"));
        //Need to work on this.
        return securityService.validatePlainRequest(request, httpHeaders, serviceName);
        //return securityClient.validatePlainRequest(request, httpHeaders,serviceName);
    }

    public Map<String, String> validateDynamicRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) throws RouterException, IOException {

        String clientId = httpHeaders.get("clientid");

        httpHeaders.put(Headers.sourceappid.name(), clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put(Headers.sourceorgid.name(), clientId.split(Constant.TILD_SPLITTER)[1]);
        MicroserviceResponse response = securityService.validate(request, httpHeaders, REQUEST.name());
        //MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypesV1.REQUEST.name());

        String userName = response.getResponse().toString();

        httpHeaders.put("username", userName);

        if(userName != null) {
            auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);
        }

        RequestValidationTypes[] requestValidationTypesArr = {APPLICATION, INACTIVE_SESSION, SESSION, IP, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            securityService.validate(request, httpHeaders, requestValidationTypes.name());
            //securityClient.validate(request, httpHeaders, requestValidationTypes.name());
        }

        return httpHeaders;
    }

    public void validatePlainDynamicRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        httpHeaders.put("clientid", httpHeaders.get("sourceOrgId") + "~" + httpHeaders.get(Headers.sourceappid.name()));

        RequestValidationTypes[] requestValidationTypes = { CLIENT_SECRET,IP};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityService.validate(request, httpHeaders, plainRequestValidation.name());
            //securityClient.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    public MicroserviceResponse validateLogout(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        return securityService.validate(request, httpHeaders, REQUEST.name());
        //return securityClient.validate(request, httpHeaders, RequestValidationTypesV1.REQUEST.name());
    }

    public Object validatePublicRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        return securityService.validatePublicRegistration(request, httpHeaders);
    }

    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        //return securityClient.validateAuthentication(request, httpHeaders);
        return securityService.validateAuthentication(request, httpHeaders);
    }


    public MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        return securityService.validateAuthenticationV2(request, httpHeaders);
    }
}
