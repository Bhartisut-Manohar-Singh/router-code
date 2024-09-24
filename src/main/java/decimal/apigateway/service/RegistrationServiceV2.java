package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.exception.RouterException;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationServiceV2 {
    Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response)throws IOException, RouterException;

    Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response, String destinationAppID)throws IOException, RouterException;

    Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response)throws RouterException;

    Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws RouterException, JsonProcessingException;
}
