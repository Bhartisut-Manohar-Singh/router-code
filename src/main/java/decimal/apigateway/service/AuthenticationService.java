package decimal.apigateway.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthenticationService {
    ResponseEntity<Object> register(String request, Map<String, String> httpHeaders);

    ResponseEntity<Object> authenticateV2(Object plainRequest, Map<String, String> httpHeaders);

    ResponseEntity<Object> forceLogout(Map<String, String> httpHeaders);

    ResponseEntity<Object> logout(Map<String, String> httpHeaders);

    ResponseEntity<Object> authenticate(Object plainRequest, Map<String, String> httpHeaders);

    ResponseEntity<Object> publicRegister(String request, Map<String, String> httpHeaders);
}
