package decimal.apigateway.service;

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
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

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
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        requestValidator.validatePlainRequest(request, httpHeaders);
        httpHeaders.put("logsrequired", "Y");
        httpHeaders.put("loginid", "random_login_id");

        return esbClient.executePlainRequest(request, httpHeaders);
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        AuditPayload auditPayload = logsWriter.initializeLog(request, httpHeaders);

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

        System.out.println("***********************************************************");
        System.out.println("Decrypted request " + decryptedResponse.getResponse());
        System.out.println("***********************************************************");
        System.out.println("Decrypted request with toString" + decryptedResponse.getResponse().toString());
        System.out.println("***********************************************************");
        ObjectNode nodes = objectMapper.createObjectNode();

        if (logRequestResponse) {
            String requestBody = decryptedResponse.getResponse().toString();//JsonMasker.maskMessage(decryptedResponse.getResponse().toString(), maskKeys);
            auditPayload.getRequest().setRequestBody(requestBody);
        } else {
            nodes.put("message", "It seems that request logs is not enabled for this api/service.");
            auditPayload.getRequest().setRequestBody(objectMapper.writeValueAsString(nodes));
        }

        Object response = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);

        if (logRequestResponse) {
            String responseBody = JsonMasker.maskMessage(objectMapper.writeValueAsString(response), maskKeys);

            auditPayload.getResponse().setResponse(responseBody);
        } else {
            nodes.put("message", "It seems that response logs is not enabled for this api/service.");
            auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(nodes));
        }

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(response, httpHeaders);

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
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

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), updateHttpHeaders);

        String requestURI = httpServletRequest.getRequestURI();

        String basePath = path + "/engine/v1/dynamic-router/" + serviceName;

        HttpHeaders httpHeaders1 = new HttpHeaders();

        updateHttpHeaders.forEach(httpHeaders1::add);

        JsonNode jsonNode = objectMapper.readValue(decryptedResponse.getResponse().toString(), JsonNode.class);

        String actualRequest = jsonNode.get("requestData").toString();

        System.out.println("Actual request is: " + actualRequest);
        HttpEntity<String> requestEntity = new HttpEntity<>(actualRequest, httpHeaders1);

        String mapping = requestURI.replaceAll(basePath, "");

        String serviceUrl = "http://" + serviceName + getContextPath(serviceName) + mapping;

        auditTraceFilter.requestIdentifier.setArn(serviceUrl);

        System.out.println("Final Url to be called is: " + serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        dynamicResponse.setStatus(Constant.SUCCESS_STATUS);
        dynamicResponse.setResponse(exchange.getBody());

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
            throw new RouterException(decryptedResponse.getResponse());
        }

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;

    }

    @Override
    public Object executeMultipartRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String uploadRequest, MultipartFile[] files) throws RouterException, IOException {

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders);

        String requestURI = httpServletRequest.getRequestURI();

        String basePath = path + "/engine/v1/dynamic-router/upload-gateway/" + serviceName;


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add(Constant.MULTIPART_FILES, new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
        }
        HttpHeaders headers = new HttpHeaders();

        headers.add("orgId", updateHttpHeaders.get("orgid"));
        headers.add("appId", updateHttpHeaders.get("appid"));

        body.add("uploadRequest", uploadRequest);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.out.println("Actual request is: " + uploadRequest);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String mapping = requestURI.replaceAll(basePath, "");

        String serviceUrl = "http://" + serviceName + getContextPath(serviceName) + mapping;

        auditTraceFilter.requestIdentifier.setArn(serviceUrl);

        System.out.println("Final Url to be called is: " + serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);


        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        if (exchange.getStatusCode().value() == 200) {
            dynamicResponse.setStatus(Constant.SUCCESS_STATUS);

        } else {
            dynamicResponse.setStatus(Constant.FAILURE_STATUS);
        }

        dynamicResponse.setResponse(exchange.getBody());

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }

    @Override
    public Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException {
        System.out.println("Actual request is: " + request);

        HttpHeaders updateHttpHeaders = new HttpHeaders();
        httpHeaders.forEach(updateHttpHeaders::set);

        HttpEntity<String> requestEntity = new HttpEntity<>(request, updateHttpHeaders);

        String requestURI = httpServletRequest.getRequestURI();

        String basePath = path + "/engine/v1/dynamic-router/plain/" + serviceName;

        String mapping = requestURI.replaceAll(basePath, "");

        String serviceUrl = "http://" + serviceName + getContextPath(serviceName) + mapping;

        System.out.println("Final Url to be called is: " + serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        dynamicResponse.setStatus(Constant.SUCCESS_STATUS);
        dynamicResponse.setResponse(exchange.getBody());

        return dynamicResponse;
    }

    private String getContextPath(String serviceName) throws RouterException {

        System.out.println("Service name is: " + serviceName.toLowerCase());
        String contextPath = "";

        List<String> services = discoveryClient.getServices();

        services.forEach(System.out::println);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toLowerCase());


        if (instances.isEmpty()) {
            throw new RouterException(Constant.FAILURE_STATUS, "Service with name: " + serviceName + " is not registered with discovery server", null);
        }

        System.out.println("Number of Instances found are: " + instances.size());

        for (ServiceInstance serviceInstance : instances) {
            Map<String, String> metadata = serviceInstance.getMetadata();
            contextPath = metadata.get("context-path");
        }

        return contextPath == null ? "" : contextPath;
    }
}
