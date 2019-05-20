package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationService
{
   Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException;

    Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException;
}
