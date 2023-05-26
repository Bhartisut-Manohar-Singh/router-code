package decimal.apigateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "loginid","deviceid", "nounce", "platform", "requestid", "requesttype", "apiname","orgid","servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme","storageid"})
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;

    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("==============================Gateway Processor=============================");
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("/execute/multipart")
    public Object executeMultipart(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("==============================Gateway Processor=============================");
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("execute/{orgId}/{appId}/{serviceName}/{version}")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String orgId,
                                      @PathVariable String appId, @PathVariable String serviceName, @PathVariable String version) throws RouterException, IOException {
        httpHeaders.put("servicename", serviceName);
        httpHeaders.put("orgid", orgId);
        httpHeaders.put("appid", appId);
        httpHeaders.put("version", version);

        System.out.println("==========================Execute=============================");
        return executionService.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("gateway")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("======================Gateway=============================");
        return executionService.executeRequest(request, httpHeaders);
    }

    @PostMapping(value = "dynamic-router/{serviceName}/**")
    public Object executeService(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        System.out.println("=============================Dynamic-router=============================");
        return executionService.executeDynamicRequest(httpServletRequest, request, httpHeaders, serviceName);
    }

    @PostMapping(value = "dynamic-router/plain/{serviceName}/**")
    public Object executeServicePlain(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        System.out.println("============================Dynamic-router/plain=============================");
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
        System.out.println("===============================Dynamic-router/upload=============================");
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
        System.out.println("===============================Dynamic-router/DMS=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeFileRequest(httpServletRequest,request,httpHeaders,serviceName,mediaDataObjects,files);

    }





}
