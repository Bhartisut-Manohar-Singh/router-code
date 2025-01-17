package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.EsbOutput;
import decimal.apigateway.service.ExecutionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Log
@RestController
@RequestMapping("engine/v1/")
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "appid", "clientid", "loginid","deviceid", "nounce", "platform", "requestid", "requesttype", "apiname","orgid","servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme","storageid", "imeinumber"})
public class ExecutionController
{
    @Autowired
    ExecutionService executionService;

    @Autowired
    ObjectMapper mapper;


    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        System.out.println("==============================Gateway Processor=============================");
        Object o = executionService.executePlainRequest(request, httpHeaders);

        EsbOutput output = mapper.convertValue(o, EsbOutput.class);

        if (output.getStatusCode()==null || output.getStatusCode().isEmpty()){
            return new ResponseEntity<>(output.getResponse(), HttpStatus.OK);
        }

        return new ResponseEntity<>(output.getResponse(), HttpStatus.valueOf(Integer.parseInt(output.getStatusCode())));
    }

    @PostMapping("callback/{serviceName}")
    public Object executePlainRequestJson(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders,@PathVariable String serviceName) throws RouterException, IOException {
        System.out.println("==============================Gateway Processor=============================");
        httpHeaders.put(Constant.SERVICE_NAME, serviceName);
        Object o = executionService.executePlainRequest(request, httpHeaders);

        EsbOutput output = mapper.convertValue(o, EsbOutput.class);

        if (output.getStatusCode()==null || output.getStatusCode().isEmpty()){
            return new ResponseEntity<>(output.getResponse(), HttpStatus.OK);
        }

        return new ResponseEntity<>(output.getResponse(), HttpStatus.valueOf(Integer.parseInt(output.getStatusCode())));
    }



    @PostMapping("execute/{orgId}/{appId}/{serviceName}/{version}")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String orgId,
                                      @PathVariable String appId, @PathVariable String serviceName, @PathVariable String version) throws RouterException, IOException {
        httpHeaders.put("servicename", serviceName);
        httpHeaders.put("orgid", orgId);
        httpHeaders.put("appid", appId);
        httpHeaders.put("version", version);

        log.info("==========================Execute=============================");
        Object o = executionService.executePlainRequest(request, httpHeaders);
        EsbOutput output = mapper.convertValue(o, EsbOutput.class);

        if (output.getStatusCode()==null || output.getStatusCode().isEmpty()){
            return new ResponseEntity<>(output.getResponse(), HttpStatus.OK);
        }

        return new ResponseEntity<>(output.getResponse(), HttpStatus.valueOf(Integer.parseInt(output.getStatusCode())));
    }

    @PostMapping("gateway")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        log.info("======================Gateway=============================");
        Object o = executionService.executeRequest(request, httpHeaders);
        EsbOutput output = mapper.convertValue(o, EsbOutput.class);

        if (output.getStatusCode()==null || output.getStatusCode().isEmpty()){
            return new ResponseEntity<>(output.getResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(output.getResponse(), HttpStatus.valueOf(Integer.parseInt(output.getStatusCode())));
    }

    @PostMapping(value = "dynamic-router/{serviceName}/**")
    public Object executeService(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        log.info("=============================Dynamic-router=============================");
        return executionService.executeDynamicRequest(httpServletRequest, request, httpHeaders, serviceName);
    }

    @PostMapping(value = "dynamic-router/plain/{serviceName}/**")
    public Object executeServicePlain(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        log.info("============================Dynamic-router/plain=============================");
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

        log.info("------------------------------------------------------------------------------------------");
        log.info("REQUEST: "+ request);
        log.info("File Size= "+files.length);
        log.info("===============================Dynamic-router/upload=============================");
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

        log.info("------------------------------------------------------------------------------------------");
        log.info("REQUEST: "+ request);
        log.info("File Size= "+files.length);
        log.info("===============================Dynamic-router/DMS=============================");
        httpHeaders.forEach((key, value) -> System.out.println(key + " " + value));
        return executionService.executeFileRequest(httpServletRequest,request,httpHeaders,serviceName,mediaDataObjects,files);

    }

}
