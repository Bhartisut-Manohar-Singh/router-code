package decimal.apigateway.service;


import decimal.apigateway.exception.PublicTokenCreationException;
import decimal.apigateway.exception.RouterExceptionV1;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface RegistrationServiceV3 {
    Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterExceptionV1, PublicTokenCreationException;
}
