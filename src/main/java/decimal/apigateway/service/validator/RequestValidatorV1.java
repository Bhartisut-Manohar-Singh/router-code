package decimal.apigateway.service.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperationsV1;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.SecurityService;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;


@Log
@Service
public class RequestValidatorV1 {

    @Autowired
    SecurityService securityService;

    @Autowired
    ObjectMapper objectMapper;

    public RequestValidatorV1(SecurityService securityService) {
        this.securityService = securityService;
    }

    public Object validateRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        log.info("=== calling validateRegistrationRequest to security client === " + new Gson().toJson(httpHeaders));
        return securityService.validateRegistration(request, httpHeaders);
    }

    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders,String serviceName) throws RouterException, IOException {
        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));
        httpHeaders.put("username", httpHeaders.get("clientid"));

        log.info("=== calling validatePlainRequest to security client === " + new Gson().toJson(httpHeaders));
        return securityService.validatePlainRequest(request, httpHeaders, serviceName);
    }

    public void validatePlainDynamicRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));

        RequestValidationTypes[] requestValidationTypes = { CLIENT_SECRET,IP};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityService.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) throws RouterException, IOException {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityService.validate(request, httpHeaders, REQUEST.name());
        try {
            log.info("====== response from security client ======= " + objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String userName = response.getResponse().toString();

        int size = RouterOperationsV1.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);

        auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

        response = securityService.validateExecutionRequest(request, httpHeaders);
        try {
            log.info(" ==== response from validateExecutionRequest ==== " + objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> customData = response.getCustomData();

        httpHeaders.put("logsrequired", customData.get("appLogs"));
        httpHeaders.put("serviceLogs", customData.get("serviceLog"));
        httpHeaders.put(Constant.KEYS_TO_MASK, customData.get(Constant.KEYS_TO_MASK));
        httpHeaders.put("logpurgedays",customData.get("logpurgedays"));

        return httpHeaders;
    }

    public Map<String, String> validateDynamicRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) throws RouterException, IOException {

        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityService.validate(request, httpHeaders, REQUEST.name());
        String userName = response.getResponse().toString();

        httpHeaders.put("username", userName);

        if(userName != null) {
            auditPayload.getRequestIdentifier().setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);
        }

        RequestValidationTypes[] requestValidationTypesArr = {APPLICATION, INACTIVE_SESSION, SESSION, IP, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr) {
            securityService.validate(request, httpHeaders, requestValidationTypes.name());
        }

        return httpHeaders;
    }

    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        return securityService.validateAuthentication(request, httpHeaders);
    }

    public MicroserviceResponse validateLogout(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        return securityService.validate(request, httpHeaders, REQUEST.name());
    }
}
