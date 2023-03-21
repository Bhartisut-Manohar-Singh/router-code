package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;
import static decimal.apigateway.enums.RequestValidationTypes.HASH;

@Service
@Log
public class RequestValidatorV2 {

    private final SecurityClient securityClient;

    @Autowired
    public RequestValidatorV2(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }


    public Object validateRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        return securityClient.validateRegistration(request, httpHeaders).getResponse();
    }

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("sourceOrgId", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("sourceAppId", clientId.split(Constant.TILD_SPLITTER)[1]);

        log.info("V2: Finally calling security client");

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());

        log.info("V2: Finally response returned by security client");

        String userName = response.getResponse().toString();

        log.info("V2: username returned -- " + userName);

        int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);

        auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

        httpHeaders.forEach((k,v) -> log.info(k + "->" + v));

        response = securityClient.validateExecutionRequestV2(request, httpHeaders);

        Map<String, String> customData = response.getCustomData();
        httpHeaders.put("destinationorgid",customData.get("destinationOrgId"));
        httpHeaders.put("logsrequired", customData.get("appLogs"));
        httpHeaders.put("serviceLogs", customData.get("serviceLog"));
        httpHeaders.put(Constant.KEYS_TO_MASK, customData.get(Constant.KEYS_TO_MASK));
        httpHeaders.put("logpurgedays",customData.get("logpurgedays"));

        return httpHeaders;

    }

    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) {
        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("sourceOrgId") + "~" + httpHeaders.get("sourceAppId"));
        httpHeaders.put("username", httpHeaders.get("clientid"));

        return securityClient.validatePlainRequest(request, httpHeaders,serviceName);
    }

    public Map<String, String> validateDynamicRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) {

        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("sourceOrgId", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("sourceAppId", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());

        String userName = response.getResponse().toString();

        httpHeaders.put("username", userName);

        if(userName != null) {
            auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);
        }

        RequestValidationTypes[] requestValidationTypesArr = {APPLICATION, INACTIVE_SESSION, SESSION, IP, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            securityClient.validate(request, httpHeaders, requestValidationTypes.name());
        }

        return httpHeaders;
    }

    public void validatePlainDynamicRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        httpHeaders.put("clientid", httpHeaders.get("sourceOrgId") + "~" + httpHeaders.get("sourceAppId"));

        RequestValidationTypes[] requestValidationTypes = { CLIENT_SECRET,IP};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityClient.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validateAuthentication(request, httpHeaders);
    }

    public MicroserviceResponse validateLogout(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());
    }
}
