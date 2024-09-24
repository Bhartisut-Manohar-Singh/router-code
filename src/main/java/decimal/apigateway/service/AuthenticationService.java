package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.exception.RouterException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

public interface AuthenticationService {
    ResponseEntity<Object> register(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    ResponseEntity<Object> authenticateV2(Object plainRequest, Map<String, String> httpHeaders) throws RouterException, IOException;

    ResponseEntity<Object> forceLogout(Map<String, String> httpHeaders);

    ResponseEntity<Object> logout(Map<String, String> httpHeaders) throws RouterException, JsonProcessingException;

    ResponseEntity<Object> authenticate(Object plainRequest, Map<String, String> httpHeaders) throws RouterException, IOException;

    ResponseEntity<Object> publicRegister(String request, Map<String, String> httpHeaders) throws RouterException, IOException;
}
