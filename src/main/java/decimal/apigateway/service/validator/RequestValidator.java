package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;

@Service
public class RequestValidator {

    private final
    SecurityClient securityClient;

    @Autowired
    LogsData logsData;

    public RequestValidator(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }

    public Object validateRegistrationRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        return securityClient.validateRegistration(request, httpHeaders).getResponse();
    }

    public void validatePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        httpHeaders.put("scopeToCheck", "PUBLIC");
        httpHeaders.put("clientid", httpHeaders.get("orgid") + "~" + httpHeaders.get("appid"));

        RequestValidationTypes[] requestValidationTypes = {HEADERS, CLIENT_SECRET, IP, SERVICE_NAME, SERVICE_SCOPE};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes) {
            securityClient.validate(request, httpHeaders, plainRequestValidation.name());
        }
    }

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders) throws RouterException {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());

        String userName = response.getResponse().toString();

        int size = RouterOperations.getStringArray(userName, Constant.TILD_SPLITTER).size();

        httpHeaders.put("username", userName);

        logsData.setLoginId(userName);
        httpHeaders.put("scopeToCheck", size > 3 ? "SECURE" : "OPEN");

        response = securityClient.validateExecutionRequest(request, httpHeaders);

        httpHeaders.put("loginid", userName.split(Constant.TILD_SPLITTER)[2]);
        httpHeaders.put("logsrequired", response.getResponse().toString());

        return httpHeaders;
    }

    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validateAuthentication(request, httpHeaders);
    }

    public MicroserviceResponse validateLogout(String request, Map<String, String> httpHeaders) throws RouterException {
        return securityClient.validate(request, httpHeaders, RequestValidationTypes.REQUEST.name());
    }
}
