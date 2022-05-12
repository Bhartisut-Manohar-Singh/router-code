package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.EsbClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.multipart.MultipartInputStreamFileResource;
import decimal.apigateway.service.validator.RequestValidator;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static decimal.apigateway.commons.Constant.*;

@Service
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    EsbClient esbClient;

    @Autowired
    SecurityClient securityClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    RestTemplate restTemplate;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    @Value("${server.servlet.context-path}")
    String path;

    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException, JsonProcessingException {

        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidator.validatePlainRequest(request, httpHeaders,httpHeaders.get("servicename"));
        JsonNode responseNode =  objectMapper.convertValue(microserviceResponse.getResponse(),JsonNode.class);
        Map<String,String> headers = objectMapper.convertValue(responseNode.get("headers"),HashMap.class);

        String logsRequired = headers.get("logsrequired");
        String serviceLog = headers.get("serviceLog");
        String keysToMask = headers.get("keys_to_mask");
        String logPurgeDays =  headers.get("logpurgedays");

        auditTraceFilter.setPurgeDays(logPurgeDays);
        httpHeaders.put("logsrequired",logsRequired);
        httpHeaders.put("serviceLogs", serviceLog);
        httpHeaders.put("loginid",headers.getOrDefault("loginid","random_login_id"));
        httpHeaders.put("logpurgedays",logPurgeDays);
        httpHeaders.put("keys_to_mask",headers.get("keys_to_mask"));
        httpHeaders.put("executionsource","API-GATEWAY");
        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));

        auditPayload.getRequest().setRequestBody(JsonMasker.maskMessage(request, maskKeys));
        auditPayload.getRequest().setHeaders(httpHeaders);


        ResponseEntity<Object> responseEntity= esbClient.executePlainRequest(request,httpHeaders);

         Object responseBody = responseEntity.getBody();

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        List<String> businessKeySet = getBusinessKey(responseBody);
        auditPayload.getResponse().setResponse(JsonMasker.maskMessage(objectMapper.writeValueAsString(responseEntity.getBody()), maskKeys));
        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        auditPayload.getResponse().setTimestamp(Instant.now());

        logsWriter.updateLog(auditPayload);

        return responseBody;
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        Map<String, String> updatedHttpHeaders = requestValidator.validateRequest(request, httpHeaders, auditPayload);

        String logsRequired = updatedHttpHeaders.get("logsrequired");
        String serviceLog = updatedHttpHeaders.get("serviceLogs");

        String keysToMask = updatedHttpHeaders.get(Constant.KEYS_TO_MASK);
        String logPurgeDays =  updatedHttpHeaders.get("logpurgedays");

        auditTraceFilter.setPurgeDays(logPurgeDays);

        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), httpHeaders);

        String maskRequestBody=JsonMasker.maskMessage(decryptedResponse.getResponse().toString(), maskKeys);
        auditPayload.getRequest().setRequestBody(maskRequestBody);

        ResponseEntity<Object> responseEntity = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);
        HttpHeaders responseHeaders = responseEntity.getHeaders();

        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        List<String> businessKeySet = getBusinessKey(responseEntity.getBody());
        String responseBody = JsonMasker.maskMessage(objectMapper.writeValueAsString(responseEntity.getBody()), maskKeys);
        auditPayload.getResponse().setResponse(responseBody);
        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);


        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(responseEntity.getBody(), httpHeaders);

        if (!SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
            auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            throw new RouterException(decryptedResponse.getResponse());
        }

        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));

        logsWriter.updateLog(auditPayload);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;
    }

    @Override
    public Object executeDynamicRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled);
        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders, auditPayload);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), updateHttpHeaders);

        String basePath = path + "/engine/v1/dynamic-router/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        HttpHeaders httpHeaders1 = new HttpHeaders();

        updateHttpHeaders.forEach(httpHeaders1::add);

        if (serviceUrl.contains("/service-executor/execute-plain"))
        {
            httpHeaders1.put("executionsource", Collections.singletonList("API-GATEWAY"));

        }
        JsonNode jsonNode = objectMapper.readValue(decryptedResponse.getResponse().toString(), JsonNode.class);

        String actualRequest = jsonNode.get("requestData").toString();


        auditPayload.getRequest().setHeaders(updateHttpHeaders);
        auditPayload.getRequest().setRequestBody(actualRequest);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setUri(serviceUrl);

        HttpEntity<String> requestEntity = new HttpEntity<>(actualRequest, httpHeaders1);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders headers = exchange.getHeaders();
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(Instant.now());
        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200 && (headers.containsKey("status") ? SUCCESS_STATUS.equalsIgnoreCase(headers.get("status").get(0)) : true)) {
            auditPayload.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");
            dynamicResponse.setStatus(SUCCESS_STATUS);

        } else {
            auditPayload.setStatus(FAILURE_STATUS);
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus(String.valueOf(exchange.getStatusCode().value()));

        }

        dynamicResponse.setResponse(exchange.getBody());


        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        if (!SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
            throw new RouterException(decryptedResponse.getResponse());
        }

        logsWriter.updateLog(auditPayload);
        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;

    }

    @Override
    public Object executeMultipartRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String uploadRequest, MultipartFile[] files) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(uploadRequest,MULTIPART, httpHeaders);

        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders,auditPayload);

        String basePath = path + "/engine/v1/dynamic-router/upload-gateway/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add(Constant.MULTIPART_FILES, new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        }
        HttpHeaders headers = new HttpHeaders();

        headers.add("orgId", updateHttpHeaders.get("orgid"));
        headers.add("appId", updateHttpHeaders.get("appid"));

        body.add("uploadRequest", uploadRequest);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        auditPayload.getRequest().setRequestBody(uploadRequest);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setHeaders(httpHeaders);
        auditPayload.getRequest().setUri(serviceUrl);



        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders responseHeaders = exchange.getHeaders();
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(Instant.now());

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200 && (responseHeaders.containsKey("status") ? SUCCESS_STATUS.equalsIgnoreCase(responseHeaders.get("status").get(0)) : true)) {
            auditPayload.setStatus(SUCCESS_STATUS);
            dynamicResponse.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");

        } else {
            auditPayload.setStatus(FAILURE_STATUS);
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus(String.valueOf(exchange.getStatusCode().value()));
        }

        dynamicResponse.setResponse(exchange.getBody());

        logsWriter.updateLog(auditPayload);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }

    @Override
    public Object executeFileRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String mediaDataObjects, MultipartFile[] files) throws RouterException, IOException {

        System.out.println("==========================================Inside DMS Service Layer=========================================" );

        auditPayload = logsWriter.initializeLog(mediaDataObjects,MULTIPART, httpHeaders);

        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders,auditPayload);

        String basePath = path + "/engine/v1/dynamic-router/upload-file/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add(Constant.MULTIPART_FILES, new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        }
        HttpHeaders headers = new HttpHeaders();

        httpHeaders.forEach((key,value)-> headers.add(key,value));

        body.add("mediaDataObjects", mediaDataObjects);

        auditPayload.getRequest().setRequestBody(mediaDataObjects);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setHeaders(httpHeaders);
        auditPayload.getRequest().setUri(serviceUrl);


        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);


        System.out.println("==========================================Calling DMS Upload Api=========================================" );

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders responseHeaders= exchange.getHeaders();
        System.out.println("==========================================Returned From DMS Upload Api=========================================" );

        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(Instant.now());

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200 && (responseHeaders.containsKey("status") ? SUCCESS_STATUS.equalsIgnoreCase(responseHeaders.get("status").get(0)) : true)) {
            auditPayload.setStatus(SUCCESS_STATUS);
            dynamicResponse.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");

        } else {
            auditPayload.setStatus(FAILURE_STATUS);
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus(String.valueOf(exchange.getStatusCode().value()));
        }



        dynamicResponse.setResponse(exchange.getBody());

        logsWriter.updateLog(auditPayload);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }


    @Override
    public Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, JsonProcessingException {

        auditPayload=logsWriter.initializeLog(request,JSON,httpHeaders);
        auditPayload.setLogRequestAndResponse(isHttpTracingEnabled);

        requestValidator.validatePlainDynamicRequest(request, httpHeaders);

        HttpHeaders updateHttpHeaders = new HttpHeaders();
        httpHeaders.forEach(updateHttpHeaders::set);


        Request requestData=new Request();
        Response responseData=new Response();
        requestData.setTimestamp(Instant.now());
        requestData.setRequestBody(objectMapper.writeValueAsString(request));
        requestData.setHeaders(updateHttpHeaders.toSingleValueMap());
        auditPayload.setRequest(requestData);

        String basePath = path + "/engine/v1/dynamic-router/plain/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        if (serviceUrl.contains("/service-executor/execute-plain"))
        {
            updateHttpHeaders.put("executionsource", Collections.singletonList("API-GATEWAY"));

        }
        HttpEntity<String> requestEntity = new HttpEntity<>(request, updateHttpHeaders);

        auditPayload.getRequestIdentifier().setArn(serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders responseHeaders = exchange.getHeaders();
         MicroserviceResponse dynamicResponse = new MicroserviceResponse();

        auditPayload.getResponse().setTimestamp(Instant.now());
        if (exchange.getStatusCode().value() == 200 && (responseHeaders.containsKey("status") ? SUCCESS_STATUS.equalsIgnoreCase(responseHeaders.get("status").get(0)) : true)) {
            auditPayload.setStatus(SUCCESS_STATUS);
            dynamicResponse.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");

        } else {
            auditPayload.setStatus(FAILURE_STATUS);
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus(String.valueOf(exchange.getStatusCode().value()));
        }

        dynamicResponse.setResponse(exchange.getBody());
        responseData.setTimestamp(Instant.now());
        responseData.setResponse(objectMapper.writeValueAsString(dynamicResponse));
        auditPayload.setRequest(requestData);
        auditPayload.setResponse(responseData);
        LogsConnector.newInstance().audit(auditPayload);

        return dynamicResponse;
    }


    private String validateAndGetServiceUrl(String serviceName, String requestURI, String basePath) throws RouterException {

        String contextPath = "";

        List<String> services = discoveryClient.getServices();

        services.forEach(System.out::println);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toLowerCase());

        if(StringUtils.isEmpty(requestURI))
        {
            throw new RouterException(FAILURE_STATUS,Constant.INVALID_URI,null);
        }

        if (instances.isEmpty()) {
            throw new RouterException(FAILURE_STATUS, "Service with name: " + serviceName + " is not registered with discovery server", null);
        }

        for (ServiceInstance serviceInstance : instances) {
            Map<String, String> metadata = serviceInstance.getMetadata();
            contextPath = metadata.get("context-path");
        }

        String mapping = requestURI.replaceAll(basePath, "");

        String serviceUrl = "http://" + serviceName + (contextPath == null ? "" : contextPath) + mapping;

        return serviceUrl;
    }

    public static List<String> getBusinessKey(Object response)
    {
        ObjectMapper objectMapper=new ObjectMapper();
        Set<String> businessKeySet = new LinkedHashSet<>();
        Map<String, Object> servicesMap=new LinkedHashMap<>();

        try {
            Map<String,Object> map = objectMapper.readValue(objectMapper.writeValueAsString(response), Map.class);


            if(map.containsKey("services")) {
                servicesMap = (Map<String, Object>) map.get("services");
            }
            else{
                servicesMap = map;
            }


            servicesMap.forEach((key, value) ->
            {
                Map<String,Object> valueMap = (Map<String, Object>) value;
                List<Map<String, Object>> recordsList = (List<Map<String, Object>>) valueMap.get("records");
                recordsList.forEach(records -> {

                            if (!StringUtils.isEmpty(records.get("primary_key")))
                                Collections.addAll(businessKeySet, records.get("primary_key").toString().split("~"));

                        }
                );
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(businessKeySet) ;

    }




}
