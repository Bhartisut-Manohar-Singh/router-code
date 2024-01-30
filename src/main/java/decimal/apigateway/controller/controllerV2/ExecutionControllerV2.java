package decimal.apigateway.controller.controllerV2;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApiAuthorizationConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.repository.SecApiAuthorizationConfigRepo;
import decimal.apigateway.service.ExecutionServiceV2;
import decimal.apigateway.service.validator.ApiAuthorizationValidator;
import decimal.ratelimiter.repo.ApiAuthorizationConfigRepo;
import lombok.extern.java.Log;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("engine/v2/")
@CrossOrigin(exposedHeaders = { "custom_headers", "security-version", "hash", "authorization", "deviceid", "sourceAppId", "clientid", "loginid","deviceid", "nounce", "platform", "requestid", "requesttype", "apiname","sourceOrgId","servicename", "txnkey", "username", "content-type", "isforcelogin" , "auth_scheme","storageid", "imeinumber"})
@Log
public class ExecutionControllerV2 {


    @Autowired
    ExecutionServiceV2 executionServiceV2;


    @PostMapping("gatewayProcessor")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders) throws RouterException, IOException {
        log.info("==============================Gateway Processor=============================");
        return executionServiceV2.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("execute/{sourceOrgId}/{sourceAppId}/{serviceName}/{version}")
    public Object executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String sourceOrgId,
                                      @PathVariable String sourceAppId, @PathVariable String serviceName, @PathVariable String version) throws RouterException, IOException {
        httpHeaders.put("servicename", serviceName);
        httpHeaders.put("orgid", sourceOrgId);
        httpHeaders.put("appid", sourceAppId);
        httpHeaders.put("version", version);

        log.info("==========================Execute=============================");
        return executionServiceV2.executePlainRequest(request, httpHeaders);
    }

    @PostMapping("gateway/{destinationAppId}/{serviceName}")
    public Object executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable(name = "destinationAppId") String destinationAppId,
                                 @PathVariable(name = "serviceName") String serviceName) throws RouterException, IOException {
        log.info("======================Gateway Execute V2 Called=============================");
        /*httpHeaders.put("sourceAppId", sourceAppId);
        httpHeaders.put("sourceOrgId", sourceOrgId);*/
        /*
        Because of network call previous header keys were in camel case.
        */
         httpHeaders.put("destinationappid", destinationAppId);
        httpHeaders.put("servicename",serviceName);

        log.info("Headers for v2 execute-----");
        httpHeaders.forEach((k,v) -> log.info(k + "->" + v));


        return executionServiceV2.executeRequest(destinationAppId,serviceName, request, httpHeaders);

    }

    @PostMapping(value = "dynamic-router/{serviceName}/**")
    public Object executeService(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        log.info("=============================Dynamic-router=============================");
        return executionServiceV2.executeDynamicRequest(httpServletRequest, request, httpHeaders, serviceName);
    }

    @PostMapping(value = "dynamic-router/plain/{serviceName}/**")
    public Object executeServicePlain(@RequestBody String request, HttpServletRequest httpServletRequest, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName) throws IOException, RouterException {

        log.info("============================Dynamic-router/plain=============================");
        return executionServiceV2.executeDynamicRequestPlain(httpServletRequest, request, httpHeaders, serviceName);
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
        return executionServiceV2.executeMultipartRequest(httpServletRequest,request,httpHeaders,serviceName,uploadRequest,files);

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

        return executionServiceV2.executeFileRequest(httpServletRequest,request,httpHeaders,serviceName,mediaDataObjects,files);
    }

}
