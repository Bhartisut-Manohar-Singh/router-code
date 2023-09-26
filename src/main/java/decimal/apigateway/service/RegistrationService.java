package decimal.apigateway.service;

import decimal.apigateway.exception.RouterExceptionV1;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationService
{
   Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterExceptionV1;

    Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterExceptionV1;

    Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws RouterExceptionV1;

    Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response);
}
