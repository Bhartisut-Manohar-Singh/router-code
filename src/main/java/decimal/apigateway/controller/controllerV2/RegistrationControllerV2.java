package decimal.apigateway.controller.controllerV2;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.RegistrationService;
import decimal.apigateway.service.RegistrationServiceV2;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v2/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "sourceAppId", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid"})
@Log
public class RegistrationControllerV2 {

    private RegistrationServiceV2 registrationServiceV2;

    @Autowired
    public RegistrationControllerV2(RegistrationServiceV2 registrationServiceV2) {
        this.registrationServiceV2 = registrationServiceV2;
    }

    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        String serviceName = httpHeaders.get("servicename");
        log.info("Service Name: " + serviceName);

        log.info("====================Call for register=============================");
        if (serviceName.contains("AUTH") || serviceName.contains("auth"))
            return registrationServiceV2.authenticate(request, httpHeaders, response);
        else if(serviceName.equalsIgnoreCase("REGISTERAPP"))
            return registrationServiceV2.register(request, httpHeaders, response);
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
        return registrationServiceV2.authenticate(request, httpHeaders, response);
    }

    @PostMapping("logout")
    public Object logout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("====================Call for logout=============================");
        return registrationServiceV2.logout(request, httpHeaders, response);
    }

    @PostMapping("forceLogout")
    public Object forceLogout(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("====================Call for force logout=============================");

        return registrationServiceV2.forceLogout(request, httpHeaders, response);
    }

}
