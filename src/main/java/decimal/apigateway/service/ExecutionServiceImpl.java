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
    LogsWriter logsWriter;


    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException, JsonProcessingException {

        AuditPayload auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        requestValidator.validatePlainRequest(request, httpHeaders);
        httpHeaders.put("logsrequired", "Y");
        httpHeaders.put("loginid", "random_login_id");
        auditPayload.getRequest().setRequestBody(request);
        auditPayload.getRequest().setHeaders(httpHeaders);

        Object objectNode= esbClient.executePlainRequest(request,httpHeaders);
        System.out.println("===========================================plain response from esb=========================");
        System.out.println(objectMapper.writeValueAsString(objectNode));
        System.out.println("===========================================plain response from esb=========================");


        List<String> businessKeySet = getBusinessKey(objectNode);
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(objectNode));

        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        auditPayload.getResponse().setTimestamp(Instant.now());

        logsWriter.updateLog(auditPayload);

        return objectNode;
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        AuditPayload auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        Map<String, String> updatedHttpHeaders = requestValidator.validateRequest(request, httpHeaders, auditPayload);

        String logsRequired = updatedHttpHeaders.get("logsrequired");
        String serviceLog = updatedHttpHeaders.get("serviceLogs");

        String keysToMask = updatedHttpHeaders.get(Constant.KEYS_TO_MASK);

        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        boolean logRequestResponse = "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), httpHeaders);


        ObjectNode nodes = objectMapper.createObjectNode();

        if (logRequestResponse) {
            String requestBody = decryptedResponse.getResponse().toString();
            String maskRequestBody=JsonMasker.maskMessage(decryptedResponse.getResponse().toString(), maskKeys);
            auditPayload.getRequest().setRequestBody(maskRequestBody);
        } else {
            nodes.put("message", "It seems that request logs is not enabled for this api/service.");
            auditPayload.getRequest().setRequestBody(objectMapper.writeValueAsString(nodes));
        }

        Object response = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);


        if (logRequestResponse) {

            List<String> businessKeySet = getBusinessKey(response);
            String responseBody = JsonMasker.maskMessage(objectMapper.writeValueAsString(response), maskKeys);
            auditPayload.getResponse().setResponse(responseBody);

            auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);


        } else {
            nodes.put("message", "It seems that response logs is not enabled for this api/service.");
            auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(nodes));
        }

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(response, httpHeaders);

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

    @Value("${server.servlet.context-path}")
    String path;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Object executeDynamicRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        AuditPayload auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), updateHttpHeaders);

        String basePath = path + "/engine/v1/dynamic-router/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        HttpHeaders httpHeaders1 = new HttpHeaders();

        updateHttpHeaders.forEach(httpHeaders1::add);


        JsonNode jsonNode = objectMapper.readValue(decryptedResponse.getResponse().toString(), JsonNode.class);

        String actualRequest = jsonNode.get("requestData").toString();


        auditPayload.getRequest().setHeaders(updateHttpHeaders);
        auditPayload.getRequest().setRequestBody(actualRequest);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setUri(serviceUrl);

        HttpEntity<String> requestEntity = new HttpEntity<>(actualRequest, httpHeaders1);



        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        dynamicResponse.setStatus(SUCCESS_STATUS);
        dynamicResponse.setResponse(exchange.getBody());


        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setStatus("200");
        auditPayload.getResponse().setTimestamp(Instant.now());


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

        AuditPayload auditPayload = logsWriter.initializeLog(uploadRequest,MULTIPART, httpHeaders);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders);

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

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200) {
            dynamicResponse.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");

        } else {
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus("200");

        }

        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(Instant.now());


        dynamicResponse.setResponse(exchange.getBody());

        logsWriter.updateLog(auditPayload);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }

    @Override
    public Object executeFileRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String mediaDataObjects, MultipartFile[] files) throws RouterException, IOException {

        AuditPayload auditPayload = logsWriter.initializeLog(mediaDataObjects,MULTIPART, httpHeaders);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders);

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


        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200) {
            dynamicResponse.setStatus(SUCCESS_STATUS);
            auditPayload.getResponse().setStatus("200");

        } else {
            dynamicResponse.setStatus(FAILURE_STATUS);
            auditPayload.getResponse().setStatus("200");
        }
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(Instant.now());


        dynamicResponse.setResponse(exchange.getBody());

        logsWriter.updateLog(auditPayload);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }


    @Override
    public Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, JsonProcessingException {

        AuditPayload auditPayload=logsWriter.initializeLog(request,JSON,httpHeaders);
        requestValidator.validatePlainDynamicRequest(request, httpHeaders);

        HttpHeaders updateHttpHeaders = new HttpHeaders();
        httpHeaders.forEach(updateHttpHeaders::set);

        HttpEntity<String> requestEntity = new HttpEntity<>(request, updateHttpHeaders);
        Request requestData=new Request();
        Response responseData=new Response();
        requestData.setTimestamp(Instant.now());
        requestData.setRequestBody(objectMapper.writeValueAsString(request));
        requestData.setHeaders(updateHttpHeaders.toSingleValueMap());
        auditPayload.setRequest(requestData);

        String basePath = path + "/engine/v1/dynamic-router/plain/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName,httpServletRequest.getRequestURI(),basePath);

        auditPayload.getRequestIdentifier().setArn(serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        dynamicResponse.setStatus(SUCCESS_STATUS);
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
