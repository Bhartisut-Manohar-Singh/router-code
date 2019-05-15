package decimal.apigateway.controller;

import decimal.apigateway.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegistrationController
{
    private RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders)
    {
        return registrationService.register(request, httpHeaders);
    }


    @PostMapping("authenticate")
    public Object authenticate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders)
    {
        return registrationService.authenticate(request, httpHeaders);
    }
}
