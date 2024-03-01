package decimal.apigateway.service.validator;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;

import java.io.IOException;
import java.util.Map;

public interface Validator {

    MicroserviceResponse validate(String request, Map<String, String> httpHeaders) throws RouterException, IOException, RouterException;
}
