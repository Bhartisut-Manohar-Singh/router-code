package decimal.apigateway.controller;

import decimal.apigateway.service.RegistrationService;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme"})
public class RegistrationController {
    private RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        String serviceName = httpHeaders.get("servicename");
        System.out.println("Service Name: " + serviceName);

        if (serviceName.contains("AUTH") || serviceName.contains("auth"))
            return registrationService.authenticate(request, httpHeaders, response);
        else
            return registrationService.register(request, httpHeaders, response);
    }


    @PostMapping("authenticate")
    public Object authenticate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        return registrationService.authenticate(request, httpHeaders, response);
    }

    @PostMapping("logout")
    public Object logout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        return registrationService.logout(request, httpHeaders, response);
    }

    @PostMapping("forceLogout")
    public Object forceLogout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {
        return registrationService.forceLogout(request, httpHeaders, response);
    }
}