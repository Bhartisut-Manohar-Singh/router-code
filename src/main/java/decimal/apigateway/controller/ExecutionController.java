package decimal.apigateway.controller;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionService;
import decimal.apigateway.service.multipart.MultipartInputStreamFileResource;
import decimal.apigateway.service.multipart.MultipartSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme"})
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;


    @Autowired
    MultipartSerive multipartSerive;

//    @Timed("apigateway_gatewayProcessor")
    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException {
        return executionService.executePlainRequest(request, httpHeaders);
    }

//    @Timed("apigateway_execute")
    @PostMapping("execute/{orgId}/{appId}/{serviceName}/{version}")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String orgId,
                                      @PathVariable String appId, @PathVariable String serviceName, @PathVariable String version) throws RouterException
    {
        httpHeaders.put("servicename", serviceName);
        httpHeaders.put("orgid", orgId);
        httpHeaders.put("appid", appId);
        httpHeaders.put("version", version);

        return executionService.executePlainRequest(request, httpHeaders);
    }

//    @Timed("apigateway_gateway")
    @PostMapping("gateway")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        return executionService.executeRequest(request, httpHeaders);
    }
}
