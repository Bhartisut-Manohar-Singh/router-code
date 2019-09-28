package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.EsbClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException {

        requestValidator.validatePlainRequest(request, httpHeaders);
        httpHeaders.put("logsrequired", "Y");
        httpHeaders.put("loginid", "random_login_id");

        return esbClient.executePlainRequest(request, httpHeaders);
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        Map<String, String> updatedHttpHeaders = requestValidator.validateRequest(request, httpHeaders);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), httpHeaders);

        Object response = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(response, httpHeaders);

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus()))
        {
            throw new RouterException(decryptedResponse.getResponse());
        }

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

        System.out.println("Final Url to be called is: " + serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        MicroserviceResponse dynamicResponse = new MicroserviceResponse();
        dynamicResponse.setStatus(Constant.SUCCESS_STATUS);
        dynamicResponse.setResponse(exchange.getBody());

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(dynamicResponse, updateHttpHeaders);

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus()))
        {
            throw new RouterException(decryptedResponse.getResponse());
        }

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;

    }

    private String getContextPath(String serviceName) throws RouterException {

        System.out.println("Service name is: " + serviceName.toLowerCase());
        String contextPath="";

        List<String> services = discoveryClient.getServices();

        services.forEach(System.out::println);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toLowerCase());


        if(instances.isEmpty()){
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
