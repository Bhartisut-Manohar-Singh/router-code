package decimal.apigateway.controller;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("engine/v2/")
public class ExecutionControllerV2 {


    @Autowired
    ExecutionServiceV2 executionServiceV2;

    @PostMapping("gateway/{destinationAppId}/{serviceName}")
    public Object executeRequest(@PathVariable String destinationAppId,@PathVariable String serviceName,
                                 @RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("======================Gateway=============================");
        /*httpHeaders.put("sourceAppId", sourceAppId);
        httpHeaders.put("sourceOrgId", sourceOrgId);*/
        httpHeaders.put("destinationAppId", destinationAppId);
        httpHeaders.put("serviceName",serviceName);
        return executionServiceV2.executeRequest(destinationAppId,serviceName, request, httpHeaders);

    }
}
