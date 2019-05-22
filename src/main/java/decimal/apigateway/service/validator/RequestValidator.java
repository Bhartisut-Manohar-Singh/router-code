package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.RequestValidationTypes;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import exception.RouterException;
import feign.Headers;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.enums.RequestValidationTypes.*;

@Service
public class RequestValidator {

    private final
    SecurityClient securityClient;

    public RequestValidator(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }

    public void validatePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        RequestValidationTypes[] requestValidationTypes = {HEADERS, CLIENT_SECRET, IP, SERVICE_NAME};

        for (RequestValidationTypes plainRequestValidation : requestValidationTypes)
        {
            MicroserviceResponse response = securityClient.validate(request, plainRequestValidation.name(), httpHeaders);

            String status = response.getStatus();

            if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
                throw new RouterException(response.getResponse());
        }
    }

    public Map<String, String> validateRequest(String request, Map<String, String> httpHeaders) throws RouterException
    {
        String clientId = httpHeaders.get("clientid");

        httpHeaders.put("orgid", clientId.split(Constant.TILD_SPLITTER)[0]);
        httpHeaders.put("appid", clientId.split(Constant.TILD_SPLITTER)[1]);

        MicroserviceResponse response = securityClient.validate(request, RequestValidationTypes.REQUEST.name(), httpHeaders);

        String status = response.getStatus();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
            throw new RouterException(response.getResponse());

        httpHeaders.put("username", response.getResponse().toString());

        RequestValidationTypes[] requestValidationTypesArr = {SESSION, IP, SERVICE_SCOPE, TXN_KEY, HASH};

        for (RequestValidationTypes requestValidationTypes : requestValidationTypesArr)
        {
            response = securityClient.validate(request, requestValidationTypes.name(), httpHeaders);

            status = response.getStatus();

            if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
                throw new RouterException(response.getResponse());
        }

        httpHeaders.put("loginid", httpHeaders.get("username").split(Constant.TILD_SPLITTER)[2]);

        return httpHeaders;
    }
}
