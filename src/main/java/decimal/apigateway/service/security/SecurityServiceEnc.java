package decimal.apigateway.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;

import java.util.Map;

public interface SecurityServiceEnc {

    MicroserviceResponse encryptResponse(String body, Map<String, String> httpHeaders) throws RouterException, JsonProcessingException;
}
