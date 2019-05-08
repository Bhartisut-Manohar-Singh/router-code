package decimal.apigateway.controller;

import decimal.apigateway.service.clients.AuthenticationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExecutionController
{
    @Autowired
    AuthenticationClient authenticationClient;

    @GetMapping("execute")
    public Object executeService()
    {
        Object response = authenticationClient.authenticate();
        System.out.println(response);

        return response;
    }

    @GetMapping("authenticate")
    public Object executeServiceAuthentication()
    {
        Object response = authenticationClient.authenticateService();
        System.out.println(response);

        return response;
    }
}
