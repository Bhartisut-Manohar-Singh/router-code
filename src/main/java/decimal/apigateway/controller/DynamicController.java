package decimal.apigateway.controller;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("dynamic-router")
public class DynamicController {

    @Autowired
    ExecutionService executionService;

    @PostMapping(value = "{serviceName}/**")
    public Object executeService(@RequestBody String request,  HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        return executionService.executeDynamicRequest(httpServletRequest, request, httpHeaders, serviceName);
    }
}
