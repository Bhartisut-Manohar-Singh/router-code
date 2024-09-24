package decimal.apigateway.service.security;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.Map;

public interface SecurityValidator {

   MicroserviceResponse validateRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    ResponseEntity validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException;

    MicroserviceResponse validateExecutionRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validateAuthenticationRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validateAuthenticationRequestV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse validatePublicRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
