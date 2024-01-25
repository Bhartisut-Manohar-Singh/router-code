package decimal.apigateway.service.security;

import decimal.apigateway.model.MicroserviceResponse;

import java.util.Map;

public interface SecurityServiceEnc {

    MicroserviceResponse encryptResponse(String body, Map<String, String> httpHeaders);
}
