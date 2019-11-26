package decimal.apigateway.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.logs.filters.AuditTraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;

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

        return securityClient.validateRegistration(request, httpHeaders).getResponse();
    }

    public void validatePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));
        httpHeaders.put("username", httpHeaders.get("clientid"));

        RequestValidationTypes[] requestValidationTypes = {HEADERS, CLIENT_SECRET, IP, SERVICE_NAME, SERVICE_SCOPE};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityClient.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders) throws RouterException {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());

        String userName = response.getResponse().toString();

        int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);

        auditTraceFilter.requestIdentifier.setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

        response = securityClient.validateExecutionRequest(request, httpHeaders);

        ObjectNode nodes = objectMapper.convertValue(response.getResponse(), ObjectNode.class);

        JsonNode appLogNode = nodes.get("appLogs");
        JsonNode serviceLogNode = nodes.get("serviceLog");

        httpHeaders.put("logsrequired", appLogNode.toString());
        httpHeaders.put("serviceLogs", serviceLogNode.toString());

        auditTraceFilter.setLogRequestAndResponse("Y".equalsIgnoreCase(appLogNode.toString()) && "Y".equalsIgnoreCase(serviceLogNode.toString()));

        return httpHeaders;
    }

    public Map<String, String> validateDynamicRequest(String request, Map<String, String> httpHeaders) {

        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());

        String userName = response.getResponse().toString();

        httpHeaders.put("username", userName);
        auditTraceFilter.requestIdentifier.setLoginId(userName.split(Constant.TILD_SPLITTER)[2]);

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
