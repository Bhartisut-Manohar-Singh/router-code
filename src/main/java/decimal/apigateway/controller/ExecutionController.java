package decimal.apigateway.controller;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme","storageid"})
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException {
        System.out.println("====================Headers for gatewayProcessor=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("execute/{orgId}/{appId}/{serviceName}/{version}")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String orgId,
                                      @PathVariable String appId, @PathVariable String serviceName, @PathVariable String version) throws RouterException
    {
        httpHeaders.put("servicename", serviceName);
        httpHeaders.put("orgid", orgId);
        httpHeaders.put("appid", appId);
        httpHeaders.put("version", version);

        System.out.println("====================Headers for execute=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("gateway")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("====================Headers for gateway=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeRequest(request, httpHeaders);
    }

    @PostMapping(value = "dynamic-router/{serviceName}/**")
    public Object executeService(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        System.out.println("====================Headers for dynamic-router=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeDynamicRequest(httpServletRequest, request, httpHeaders, serviceName);
    }

    @PostMapping(value = "dynamic-router/plain/{serviceName}/**")
    public Object executeServicePlain(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        System.out.println("====================Headers for dynamic-router/plain=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeDynamicRequestPlain(httpServletRequest, request, httpHeaders, serviceName);
    }

    @PostMapping(value = "dynamic-router/upload-gateway/{serviceName}/**",consumes = "multipart/form-data")
    public Object executeMultipartRequest(
            @RequestPart String request,
            @RequestHeader Map<String, String> httpHeaders,
            @PathVariable String serviceName,
            @RequestPart(Constant.MULTIPART_FILES) MultipartFile[] files,
            @RequestPart("uploadRequest") String uploadRequest,
            HttpServletRequest httpServletRequest) throws Exception {

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("REQUEST: "+ request);
        System.out.println("File Size= "+files.length);
        System.out.println("====================Headers for dynamic-router/upload=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeMultipartRequest(httpServletRequest,request,httpHeaders,serviceName,uploadRequest,files);

    }

    @PostMapping(value = "dynamic-router/upload-file/{serviceName}/**",consumes = "multipart/form-data")
    public Object executeFileRequest(
            @RequestPart String request,
            @RequestHeader Map<String, String> httpHeaders,
            @PathVariable String serviceName,
            @RequestPart(Constant.MULTIPART_FILES) MultipartFile[] files,
            @RequestPart("mediaDataObjects") String mediaDataObjects,
            HttpServletRequest httpServletRequest) throws Exception {

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("REQUEST: "+ request);
        System.out.println("File Size= "+files.length);
        System.out.println("====================Headers for dynamic-router/DMS=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeFileRequest(httpServletRequest,request,httpHeaders,serviceName,mediaDataObjects,files);

    }





}
