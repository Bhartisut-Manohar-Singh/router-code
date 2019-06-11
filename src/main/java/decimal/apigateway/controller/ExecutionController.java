package decimal.apigateway.controller;

import decimal.apigateway.service.ExecutionService;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme"})
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException {
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("gateway")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        return executionService.executeRequest(request, httpHeaders);
    }
}
