package decimal.apigateway.controller.V4;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.serviceV3.RegistrationServiceV4;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v4/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid"})
@Log
public class RegistrationControllerV4 {
    private RegistrationServiceV4 registrationServiceV4;

    @Autowired
    public RegistrationControllerV4(RegistrationServiceV4 registrationServiceV4) {
        this.registrationServiceV4 = registrationServiceV4;
    }


    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        String serviceName = httpHeaders.get("servicename");
        log.info("Service Name: " + serviceName);

        log.info("====================Call for register=============================");
        if (serviceName.contains("AUTH") || serviceName.contains("auth"))
            return registrationServiceV4.authenticate(request, httpHeaders, response);
        else if(serviceName.equalsIgnoreCase("REGISTERAPP"))
            return registrationServiceV4.register(request, httpHeaders, response);
        else
        {
            String message = Constant.INVALID_SERVICE_NAME;
            String status = Constant.FAILURE_STATUS;
            MicroserviceResponse microserviceResponse = new MicroserviceResponse(status, message, "");

            throw new RouterException(Constant.ROUTER_SERVICE_NOT_FOUND,  Constant.ROUTER_ERROR_TYPE_VALIDATION,microserviceResponse);
        }
    }

}
