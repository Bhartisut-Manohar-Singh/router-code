package decimal.apigateway.service.serviceV3;

import decimal.apigateway.exception.RouterException;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationServiceV4
{
   Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException;

    Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException;

}
