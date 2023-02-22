package decimal.apigateway.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;


@Log
@Service
public class RequestValidator {

    private final
    SecurityClient securityClient;

    @Autowired
    ObjectMapper objectMapper;

    public RequestValidator(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }

    public Object validateRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException {
        log.info("=== calling validateRegistrationRequest to security client === " + new Gson().toJson(httpHeaders));
        return securityClient.validateRegistration(request, httpHeaders).getResponse();
    }

    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders,String serviceName) throws RouterException {
        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));
        httpHeaders.put("username", httpHeaders.get("clientid"));

        log.info("=== calling validatePlainRequest to security client === " + new Gson().toJson(httpHeaders));
        return securityClient.validatePlainRequest(request, httpHeaders,serviceName);

    }

    public void validatePlainDynamicRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));

        RequestValidationTypes[] requestValidationTypes = { CLIENT_SECRET,IP};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityClient.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) throws RouterException {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());
        try {
            log.info("====== response from security client ======= " + objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String userName = response.getResponse().toString();

        int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);

        auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

        response = securityClient.validateExecutionRequest(request, httpHeaders);

        Map<String, String> customData = response.getCustomData();

        httpHeaders.put("logsrequired", customData.get("appLogs"));
        httpHeaders.put("serviceLogs", customData.get("serviceLog"));
        httpHeaders.put(Constant.KEYS_TO_MASK, customData.get(Constant.KEYS_TO_MASK));
        httpHeaders.put("logpurgedays",customData.get("logpurgedays"));

        return httpHeaders;
    }

    public Map<String, String> validateDynamicRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) {

        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

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

    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validateAuthentication(request, httpHeaders);
    }

    public MicroserviceResponse validateLogout(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());
    }
}
