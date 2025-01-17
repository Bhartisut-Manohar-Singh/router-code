package decimal.apigateway.controller;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.RegistrationService;
import decimal.apigateway.service.serviceV3.RegistrationServiceV4;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid"})
@Log
public class RegistrationController {
    private RegistrationService registrationService;

    @Autowired
    RegistrationServiceV4 registrationServiceV4;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }


    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        String serviceName = httpHeaders.get("servicename");
        String loginType = httpHeaders.getOrDefault("auth_method","*");
        log.info("Service Name: " + serviceName);
        log.info("Login Type: "+ loginType);

        log.info("====================Call for register=============================");
        if (serviceName.contains("AUTH") || serviceName.contains("auth"))
            if (Constant.AUTH_SSO.equalsIgnoreCase(loginType)){
                return registrationServiceV4.authenticate(request, httpHeaders, response);
            } else {
                return registrationService.authenticate(request, httpHeaders, response);
            }
        else if(serviceName.equalsIgnoreCase("REGISTERAPP"))
            return registrationService.register(request, httpHeaders, response);
        else
        {
            String message = Constant.INVALID_SERVICE_NAME;
            String status = Constant.FAILURE_STATUS;
            MicroserviceResponse microserviceResponse = new MicroserviceResponse(status, message, "");

            throw new RouterException(Constant.ROUTER_SERVICE_NOT_FOUND,  Constant.ROUTER_ERROR_TYPE_VALIDATION,microserviceResponse);

        }

          // return new ResponseEntity<>( Constant.ROUTER_SERVICE_NOT_FOUND
//, HttpStatus.BAD_REQUEST);

    }

    @PostMapping("authenticate")
    public Object authenticate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("====================Call for authenticate=============================");
        return registrationService.authenticate(request, httpHeaders, response);
    }

    @PostMapping("logout")
    public Object logout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("====================Call for logout=============================");
        return registrationService.logout(request, httpHeaders, response);
    }

    @PostMapping("forceLogout")
    public Object forceLogout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {
        log.info("====================Call for force logout=============================");

        return registrationService.forceLogout(request, httpHeaders, response);
    }
}
