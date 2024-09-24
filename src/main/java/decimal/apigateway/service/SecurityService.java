package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public interface SecurityService {

    Object validateRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validateExecutionRequestV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse decryptRequest(JsonNode node, Map<String, String> httpHeaders) throws RouterException;



    MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException;

    MicroserviceResponse encryptResponseWithoutSession(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) throws RouterException;

    MicroserviceResponse generateResponseHash(String finalResponse, Map<String, String> httpHeaders) throws RouterException;

    MicroserviceResponse validate(String request, Map<String, String> httpHeaders, String name) throws RouterException, IOException;

    MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse generateAuthResponseHash(String string, Map<String, String> httpHeaders) throws RouterException;

    MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException;

    MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    Object validatePublicRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
