package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.sessionmanagement.exception.RouterException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public interface SecurityService {

    Object validateRegistration(String request, Map<String, String> httpHeaders);

    MicroserviceResponse validateExecutionRequestV2(String request, Map<String, String> httpHeaders);

    MicroserviceResponse decryptRequest(JsonNode node, Map<String, String> httpHeaders);

    MicroserviceResponse encryptResponse(String body, Map<String, String> httpHeaders);

    MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders);

    MicroserviceResponse encryptResponseWithoutSession(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders);

    MicroserviceResponse generateResponseHash(String finalResponse, Map<String, String> httpHeaders);

    MicroserviceResponse validate(String request, Map<String, String> httpHeaders, String name);

    MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders);

    MicroserviceResponse generateAuthResponseHash(String string, Map<String, String> httpHeaders);

    MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName);

    MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders);

    Object validatePublicRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders);
}
