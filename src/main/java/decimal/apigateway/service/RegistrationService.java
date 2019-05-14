package decimal.apigateway.service;

import java.util.Map;

public interface RegistrationService
{
   Object register(String request, Map<String, String> httpHeaders);
}
