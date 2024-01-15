package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;

import java.io.IOException;
import java.util.Map;

public interface AuthenticationProcessor {

    MicroserviceResponse register(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse authenticate(String request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse authenticateV2(Object request, Map<String, String> httpHeaders) throws RouterException, IOException;

    MicroserviceResponse logout(Map<String, String> httpHeaders) throws RouterException, JsonProcessingException;

    MicroserviceResponse forceLogout(Map<String, String> httpHeaders);

    MicroserviceResponse registerDevice(String request, Map<String, String> httpHeaders) throws IOException, RouterException;

    MicroserviceResponse authenticateUser(String request, Map<String, String> httpHeaders) throws IOException, RouterException;


    MicroserviceResponse publicRegister(String request, Map<String, String> httpHeaders) throws IOException, RouterException;
}
