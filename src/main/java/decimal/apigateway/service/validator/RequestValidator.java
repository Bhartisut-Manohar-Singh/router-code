package decimal.apigateway.service.validator;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.PlainRequestValidationTypes;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.SecurityClient;
import exception.RouterException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RequestValidator {

    private final
    SecurityClient securityClient;

    public RequestValidator(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }

    public void validatePlainRequest(Map<String, String> httpHeaders) throws RouterException {

        for (PlainRequestValidationTypes plainRequestValidation : PlainRequestValidationTypes.values())
        {
            MicroserviceResponse response = securityClient.validate(plainRequestValidation.name(), httpHeaders);

            String status = response.getStatus();

            if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
                throw new RouterException(response.getResponse());
        }
    }
}
