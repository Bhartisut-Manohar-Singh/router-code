package decimal.apigateway.controller.V3;


import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionService;
import decimal.apigateway.service.RegistrationServiceV3;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v3/")
@CrossOrigin(exposedHeaders = {"custom_headers", "security-version", "hash", "authorization", "deviceid", "sourceAppId", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin", "auth_scheme","storageid", "imeinumber"})
@Log
public class RegistrationControllerV3 {

    private final RegistrationServiceV3 registrationServiceV3;

    private final ExecutionService executionService;

    @Autowired
    public RegistrationControllerV3(RegistrationServiceV3 registrationServiceV3, ExecutionService executionService) {
        this.registrationServiceV3 = registrationServiceV3;
        this.executionService = executionService;
    }

    /**
     * Registation API for Public Requests.
     * @param request
     * @param httpHeaders
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("register")
    public Object executeService(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        return registrationServiceV3.register(request, httpHeaders, response);
    }

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws  IOException, RouterException {
        log.info("==============================Public Gateway Processor=============================");
        return executionService.executePlainRequest(request, httpHeaders);
    }

}
