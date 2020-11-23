package decimal.apigateway.controller;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid","orgid", "appid","clientsecret","storageid", "loginid","clientid", "deviceid", "nounce", "platform", "requestid", "requesttype", "servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme"})
public class MultiPartController
{
    @Autowired
    ExecutionService executionService;

    @PostMapping(value = "dynamic-router/{serviceName}/**",consumes = "multipart/form-data")
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

