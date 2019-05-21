package decimal.apigateway.controller;

import decimal.apigateway.service.ExecutionService;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException {
        return executionService.executePlainRequest(request, httpHeaders);
    }
}
