package decimal.apigateway.service;

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

    @Autowired
    public RegistrationServiceImpl(SecurityClient securityClient, AuthenticationClient authenticationClient) {
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders) {
        MicroserviceResponse response = securityClient.validateRegistration(request, httpHeaders);

/*
        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(response.getStatus()))
        {
            //TODO - Return invalid response according to registration failure
        }*/

        return authenticationClient.register(request, httpHeaders);
    }

    @Override
    public Object authenticate(String request, Map<String, String> httpHeaders)
    {
        MicroserviceResponse response = securityClient.validateAuthentication(request, httpHeaders);

        return null;
    }
}
