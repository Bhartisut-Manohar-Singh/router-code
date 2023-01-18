package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.logs.model.AuditPayload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RequestValidatorV2 {

    private final SecurityClient securityClient;

    public RequestValidatorV2(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }
    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders, AuditPayload auditPayload) {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("sourceOrgId", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("sourceAppId", clientId.split(Constant.TILD_SPLITTER)[1]);



        MicroserviceResponse response =securityClient.validateExecutionRequestV2(request, httpHeaders);
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
}
