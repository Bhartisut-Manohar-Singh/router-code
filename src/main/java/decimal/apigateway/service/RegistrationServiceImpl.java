package decimal.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.AuthenticationClient;
import decimal.apigateway.service.clients.SecurityClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private SecurityClient securityClient;

    private AuthenticationClient authenticationClient;

    private ObjectMapper objectMapper;

    @Autowired
    public RegistrationServiceImpl(SecurityClient securityClient, AuthenticationClient authenticationClient, ObjectMapper objectMapper) {
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders) {
        MicroserviceResponse response = securityClient.validateRegistration(request, httpHeaders);

/*
        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(response.getStatus()))
        {
            //TODO - Return invalid response according to registration failure
        }*/

        ObjectNode jsonNodes = objectMapper.convertValue(response.getResponse(), ObjectNode.class);

        String userName = jsonNodes.get("username").asText();

        httpHeaders.put("username", userName);

        return authenticationClient.register(request, httpHeaders);
    }

    @Override
    public Object authenticate(String request, Map<String, String> httpHeaders)
    {
        MicroserviceResponse response = securityClient.validateAuthentication(request, httpHeaders);

        return null;
    }
}
