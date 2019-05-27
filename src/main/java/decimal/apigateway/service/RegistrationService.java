package decimal.apigateway.service;

import exception.RouterException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationService
{
   Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException;

    Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException;

    Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws RouterException;

    Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response);
}
