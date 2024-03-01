package decimal.apigateway.service.security;

import decimal.apigateway.exception.RouterException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public interface ValidationServiceV2 {
    ResponseEntity validateExecutionRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
