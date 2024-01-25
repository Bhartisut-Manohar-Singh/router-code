package decimal.apigateway.service.security;


import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;

import java.util.Map;

public interface EncryptionDecryptionService {
    MicroserviceResponse generateResponseHash(String request, Map<String, String> httpHeaders) throws RouterException;
    MicroserviceResponse decryptRequest(String request, Map<String, String> httpHeaders) throws RouterException;
    MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException;
    MicroserviceResponse generateAuthResponseHash(String body, Map<String, String> httpHeaders) throws RouterException;
    MicroserviceResponse encryptResponse(String request, Map<String, String> httpHeaders) throws RouterException;
    MicroserviceResponse encryptResponseWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException;
}
