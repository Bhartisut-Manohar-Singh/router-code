package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.apigateway.clients.VahanaDMSClient;
import decimal.apigateway.commons.Constant;

import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.EsbOutput;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.multipart.MultipartInputStreamFileResource;
import decimal.apigateway.service.security.SecurityServiceEnc;
import decimal.apigateway.service.util.ServiceInstanceUtil;
import decimal.apigateway.service.validator.RequestValidatorV1;
import decimal.apigateway.clients.EsbClientAuth;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import lombok.extern.java.Log;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    RequestValidatorV1 requestValidator;

    @Autowired
    EsbClientAuth esbClient;

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

    @Autowired
    VahanaDMSClient vahanaDMSClient;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    @Value("${server.servlet.context-path}")
    String path;

    @Value("${connectionTimeout}")
    int connectionTimeout;

    @Value("${readTimeout}")
    int readTimeout;

    @Autowired
    SecurityServiceEnc securityServiceEnc;

    @Autowired
    SecurityService securityService;


    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("==== inside executePlainRequest ==== ");
        auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidator.validatePlainRequest(request, httpHeaders, httpHeaders.get("servicename"));
        JsonNode responseNode = objectMapper.convertValue(microserviceResponse.getResponse(), JsonNode.class);
        //Map<String, String> headers = objectMapper.convertValue(responseNode.get("headers"), HashMap.class);
        Map<String,Object> response = objectMapper.convertValue(responseNode.get("response"),HashMap.class);
        log.info("==== inside executePlainRequest ====  response "+response);

        log.info("==== inside executePlainRequest ====  headers "+response.get("headers"));
        Map<String,String> headers = (Map<String, String>) response.get("headers");

        log.info("=== calling execute plain request -------=== " + new Gson().toJson(headers));
        String isDigitallySigned = headers.get(IS_DIGITALLY_SIGNED);
        String isPayloadEncrypted = headers.get(IS_PAYLOAD_ENCRYPTED);

        if (("Y").equalsIgnoreCase(isPayloadEncrypted) || ("Y").equalsIgnoreCase(isDigitallySigned)) {
            if (!httpHeaders.containsKey(ROUTER_HEADER_SECURITY_VERSION))
                httpHeaders.put(ROUTER_HEADER_SECURITY_VERSION, "2");

            JsonNode node = objectMapper.readValue(request, JsonNode.class);

            if (("Y").equalsIgnoreCase(isPayloadEncrypted) && (!node.hasNonNull("request")))
                throw new RouterException(INVALID_REQUEST_500, "Please send a valid request", objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send encrypted payload.\"}"));

            if (("Y").equalsIgnoreCase(isPayloadEncrypted) && !httpHeaders.containsKey(Headers.txnkey.name()))
                throw new RouterException(INVALID_REQUEST_500, "Please send a valid request", objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send txnkey in HeadersV1 as your request payload is encrypted.\"}"));

            if (("Y").equalsIgnoreCase(isDigitallySigned) && !httpHeaders.containsKey(Headers.hash.name()))
                throw new RouterException(INVALID_REQUEST_500, "Please send a valid request", objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send hash in HeadersV1 as your request is digitally signed.\"}"));

            httpHeaders.put(IS_DIGITALLY_SIGNED, isDigitallySigned);
            httpHeaders.put(IS_PAYLOAD_ENCRYPTED, isPayloadEncrypted);
            httpHeaders.put(Headers.clientsecret.name(), headers.get(Headers.clientsecret.name()));

            MicroserviceResponse decryptedResponse = securityService.decryptRequestWithoutSession(("Y").equalsIgnoreCase(isPayloadEncrypted) ? node.get("request").asText() : request, httpHeaders);

            if (("Y").equalsIgnoreCase(isPayloadEncrypted))
                request = decryptedResponse.getResponse().toString();
        }

        String logsRequired = headers.get("logsrequired");
        String serviceLog = headers.get("serviceLog");
        String keysToMask = headers.get("keys_to_mask");
        String logPurgeDays = headers.get("logpurgedays");

        auditTraceFilter.setPurgeDays(logPurgeDays);
        httpHeaders = setHeaders(httpHeaders, headers, logsRequired, serviceLog, logPurgeDays);
        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));
        auditPayload.getRequest().setRequestBody(JsonMasker.maskMessage(request, maskKeys));
        auditPayload.getRequest().setHeaders(httpHeaders);


        log.info(" ======= calling esb ======= ");
        log.info(" ======= request ======= " + request);
        log.info(" ======= headers ======= " + objectMapper.writeValueAsString(httpHeaders));
        httpHeaders.remove("content-length");
        ResponseEntity<Object> responseEntity = esbClient.executePlainRequest(request, httpHeaders);

        Object responseBody = responseEntity.getBody();

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        String statusCode = "";

        if (responseHeaders != null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));


        if (responseHeaders != null && responseHeaders.containsKey("statuscode")) {
            statusCode = responseHeaders.get("statuscode").get(0);
        }

        log.info(" ===== response Body from esb ===== " + new Gson().toJson(responseBody));
        List<String> businessKeySet = getBusinessKey(responseBody);
        auditPayload.getResponse().setResponse(new Gson().toJson(responseEntity.getBody()));
        auditPayload.getRequestIdentifier().setBusinessFilter(businessKeySet);
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

        logsWriter.updateLog(auditPayload);

        if (("Y").equalsIgnoreCase(isPayloadEncrypted)) {
            MicroserviceResponse encryptedResponse = securityService.encryptResponseWithoutSession(responseEntity, httpHeaders);
            Map<String, String> finalResponseMap = new HashMap<>();
            finalResponseMap.put("response", encryptedResponse.getMessage());

            EsbOutput output = new EsbOutput();
            output.setResponse(finalResponseMap);
            setStatusCodeIfPresent(statusCode, output);

            return output;
        }
        EsbOutput output = new EsbOutput();
        output.setResponse(responseBody);
        setStatusCodeIfPresent(statusCode, output);

        return output;
    }

    public static void setStatusCodeIfPresent(String statusCode, EsbOutput output) {

        if (!statusCode.isEmpty() && statusCode.chars().allMatch(Character::isDigit))
            output.setStatusCode(statusCode);
    }

    private static Map<String, String> setHeaders(Map<String, String> httpHeaders, Map<String, String> headers, String logsRequired, String serviceLog, String logPurgeDays) {
        httpHeaders.put("logsrequired", logsRequired);
        httpHeaders.put("serviceLogs", serviceLog);
        httpHeaders.put("loginid", headers.getOrDefault("loginid", "vahana"));
        httpHeaders.put("logpurgedays", logPurgeDays);
        httpHeaders.put("keys_to_mask", headers.get("keys_to_mask"));
        httpHeaders.put("executionsource", "API-GATEWAY");

        return httpHeaders;
    }

    @Override
    public Object executeRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("==== inside execute request === ");
        auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders);

        Map<String, String> updatedHttpHeaders = requestValidator.validateRequest(request, httpHeaders, auditPayload);

        String logsRequired = updatedHttpHeaders.get("logsrequired");
        String serviceLog = updatedHttpHeaders.get("serviceLogs");

        String keysToMask = updatedHttpHeaders.get(Constant.KEYS_TO_MASK);
        String logPurgeDays = updatedHttpHeaders.get("logpurgedays");

        auditTraceFilter.setPurgeDays(logPurgeDays);

        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityService.decryptRequest(node.get("request"), httpHeaders);
        log.info(" ==== decrypted response ==== " + objectMapper.writeValueAsString(decryptedResponse));

        String maskRequestBody = JsonMasker.maskMessage(decryptedResponse.getResponse().toString(), maskKeys);
        auditPayload.getRequest().setRequestBody(maskRequestBody);
        updatedHttpHeaders.put("executionsource", "API-GATEWAY");

        updatedHttpHeaders.remove("content-length");
        ResponseEntity<Object> responseEntity = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);
        HttpHeaders responseHeaders = responseEntity.getHeaders();

        String statusCode = "";

        if (responseHeaders != null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        if (responseHeaders != null && responseHeaders.containsKey("statuscode")) {
            statusCode = responseHeaders.get("statuscode").get(0);
        }

        log.info(" ===== response Body from esb ===== " + objectMapper.writeValueAsString(responseEntity.getBody()));
        List<String> businessKeySet = getBusinessKey(responseEntity.getBody());
        String responseBody = JsonMasker.maskMessage(objectMapper.writeValueAsString(responseEntity.getBody()), maskKeys);
        auditPayload.getResponse().setResponse(responseBody);
        auditPayload.getRequestIdentifier().setBusinessFilter(businessKeySet);
        httpHeaders.put("executionsource", "API-GATEWAY");

        log.info("==== body and headers ====" + objectMapper.writeValueAsString(responseEntity.getBody()) + " " + objectMapper.writeValueAsString(httpHeaders));
        MicroserviceResponse encryptedResponse = securityServiceEnc.encryptResponse(objectMapper.writeValueAsString(responseEntity.getBody()), httpHeaders);
        log.info("==== encryptedResponse ====1 " + objectMapper.writeValueAsString(encryptedResponse));

        if (!SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
            auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()));

            throw new RouterException(decryptedResponse.getResponse());
        }
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        logsWriter.updateLog(auditPayload);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());
        EsbOutput esbOutput = new EsbOutput();
        esbOutput.setResponse(finalResponseMap);
        setStatusCodeIfPresent(statusCode, esbOutput);
        return esbOutput;
    }

    @Override
    public Object executeDynamicRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders);

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);
        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders, auditPayload);

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityService.decryptRequest(node.get("request"), updateHttpHeaders);

        String basePath = path + "/engine/v1/dynamic-router/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName, httpServletRequest.getRequestURI(), basePath, true);

        HttpHeaders httpHeaders1 = new HttpHeaders();

        updateHttpHeaders.forEach(httpHeaders1::add);

        JsonNode jsonNode = objectMapper.readValue(decryptedResponse.getResponse().toString(), JsonNode.class);

        String actualRequest = jsonNode.get("requestData").toString();

        auditPayload.getRequest().setHeaders(updateHttpHeaders);
        auditPayload.getRequest().setRequestBody(actualRequest);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setUri(serviceUrl);

        httpHeaders1.remove("content-length");
        httpHeaders1.put("executionsource", Collections.singletonList("API-GATEWAY"));

        HttpEntity<String> requestEntity = new HttpEntity<>(actualRequest, httpHeaders1);
        log.info(" ==== Dyanmic Router URL ====" + serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders headers = exchange.getHeaders();
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
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

        MicroserviceResponse encryptedResponse = securityServiceEnc.encryptResponse(objectMapper.writeValueAsString(dynamicResponse), updateHttpHeaders);

        if (!SUCCESS_STATUS.equalsIgnoreCase(decryptedResponse.getStatus())) {
            throw new RouterException(decryptedResponse.getResponse());
        }

        if(!VMONITOR_SERVICE_NAME.equalsIgnoreCase(serviceName))
            logsWriter.updateLog(auditPayload);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;

    }

    @Override
    public Object executeMultipartRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String uploadRequest, MultipartFile[] files) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(uploadRequest, MULTIPART, httpHeaders);

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders, auditPayload);

        String basePath = path + "/engine/v1/dynamic-router/upload-gateway/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName, httpServletRequest.getRequestURI(), basePath, false);

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


        headers.put("executionsource", Collections.singletonList("API-GATEWAY"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate multipartRestTemplate = getRestTemplate();

        ResponseEntity<Object> exchange = multipartRestTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders responseHeaders = exchange.getHeaders();
        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

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

        MicroserviceResponse encryptedResponse = securityServiceEnc.encryptResponse(dynamicResponse.getResponse().toString(), updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }

    @Override
    public Object executeFileRequest(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName, String mediaDataObjects, MultipartFile[] files) throws RouterException, IOException {

        log.info("==========================================Inside DMS Service Layer=========================================");

        auditPayload = logsWriter.initializeLog(mediaDataObjects, MULTIPART, httpHeaders);

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        Map<String, String> updateHttpHeaders = requestValidator.validateDynamicRequest(request, httpHeaders, auditPayload);

        HttpHeaders headers = new HttpHeaders();
        httpHeaders.forEach((key, value) -> headers.add(key, value));


        auditPayload.getRequest().setRequestBody(mediaDataObjects);
        auditPayload.getRequest().setMethod("POST");
        auditPayload.getRequest().setHeaders(httpHeaders);
        auditPayload.getRequest().setUri(httpServletRequest.getRequestURI());

        headers.put("executionsource", Collections.singletonList("API-GATEWAY"));

        headers.remove("content-length");

        System.out.println("==========================================Calling DMS Upload Api=========================================");

        ResponseEntity<Object> exchange = httpServletRequest.getRequestURI().endsWith("uploadDocument") ? vahanaDMSClient.uploadFile(headers,mediaDataObjects,files) : vahanaDMSClient.uploadAndGetSignedUrl(headers,mediaDataObjects,files);

        HttpHeaders responseHeaders = exchange.getHeaders();
        System.out.println("==========================================Returned From DMS Upload Api=========================================");

        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(exchange.getBody()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

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

        MicroserviceResponse encryptedResponse = securityServiceEnc.encryptResponse(objectMapper.writeValueAsString(dynamicResponse), updateHttpHeaders);

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        return finalResponseMap;


    }


    @Override
    public Object executeDynamicRequestPlain(HttpServletRequest httpServletRequest, String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders);
        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        requestValidator.validatePlainDynamicRequest(request, httpHeaders);

        HttpHeaders updateHttpHeaders = new HttpHeaders();
        httpHeaders.forEach(updateHttpHeaders::set);


        Request requestData = new Request();
        Response responseData = new Response();
        requestData.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        requestData.setRequestBody(objectMapper.writeValueAsString(request));
        requestData.setHeaders(updateHttpHeaders.toSingleValueMap());
        auditPayload.setRequest(requestData);

        String basePath = path + "/engine/v1/dynamic-router/plain/" + serviceName;

        String serviceUrl = validateAndGetServiceUrl(serviceName, httpServletRequest.getRequestURI(), basePath, true);

        if (serviceUrl.contains("/service-executor/execute-plain")) {
            updateHttpHeaders.put("executionsource", Collections.singletonList("API-GATEWAY"));

        }

        log.info("===============================Dyanmic Router Plain URL==========================");
        log.info(serviceUrl);

        updateHttpHeaders.put("executionsource", Collections.singletonList("API-GATEWAY"));

        HttpEntity<String> requestEntity = new HttpEntity<>(request, updateHttpHeaders);

        auditPayload.getRequestIdentifier().setArn(serviceUrl);

        ResponseEntity<Object> exchange = restTemplate.exchange(serviceUrl, HttpMethod.POST, requestEntity, Object.class);

        HttpHeaders responseHeaders = exchange.getHeaders();
        MicroserviceResponse dynamicResponse = new MicroserviceResponse();

        auditPayload.getResponse().setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
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
        responseData.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        responseData.setResponse(objectMapper.writeValueAsString(dynamicResponse));
        auditPayload.setRequest(requestData);
        auditPayload.setResponse(responseData);
        LogsConnector.newInstance().audit(auditPayload);

        return dynamicResponse;
    }


    private String validateAndGetServiceUrl(String serviceName, String requestURI, String basePath, Boolean isDynamic) throws RouterException, JsonProcessingException {

        String contextPath = "";
        int port = 0;

        List<String> services = discoveryClient.getServices();

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName.toLowerCase());
        try {
            log.info(" ======= services ======= " + objectMapper.writeValueAsString(instances));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.isEmpty(requestURI)) {
            throw new RouterException(FAILURE_STATUS, Constant.INVALID_URI, null);
        }

        if (instances.isEmpty()) {
            throw new RouterException(FAILURE_STATUS, "Service with name: " + serviceName + " is not registered with discovery server", null);
        }

        for (ServiceInstance serviceInstance : instances) {
            contextPath = getContextPath(serviceInstance);
            log.info(" === context path === " + contextPath);
            port = serviceInstance.getPort();
        }

        String mapping = requestURI.replaceAll(basePath, "");
        if (contextPath != null) {
            if (mapping.contains(contextPath)) {
                return "http://" + serviceName.toLowerCase() + ":" + port + mapping;
            }
        }
        log.info(" === mapping === " + mapping);
        return "http://" + serviceName + ":" + port + (contextPath == null ? "" : contextPath) + mapping;
    }

    private String getContextPath(ServiceInstance serviceInstance) throws JsonProcessingException {
        ServiceInstanceUtil serviceInstanceUtil = objectMapper.convertValue(serviceInstance, ServiceInstanceUtil.class);
        try {
            log.info(" ==== serviceInstanceUtil ====" + objectMapper.writeValueAsString(serviceInstanceUtil));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if(serviceInstanceUtil.getTags() != null && !serviceInstanceUtil.getTags().isEmpty()){
            String contextPath = null;
            for(String tags : serviceInstanceUtil.getTags())
            {
                if(tags!=null && tags.contains("contextPath")) {
                    contextPath = tags.split("=")[1];
                    break;
                }
            }

            return contextPath;
        }
        else
        {
            Map<String, String> metadata = serviceInstance.getMetadata();
            return metadata.get("context-path") != null ? metadata.get("context-path") : null;
        }
    }

    public static List<String> getBusinessKey(Object response) {
        ObjectMapper objectMapper = new ObjectMapper();
        Set<String> businessKeySet = new LinkedHashSet<>();
        Map<String, Object> servicesMap = new LinkedHashMap<>();

        try {
            Map<String, Object> map = objectMapper.readValue(objectMapper.writeValueAsString(response), Map.class);


            if (map.containsKey("services")) {
                servicesMap = (Map<String, Object>) map.get("services");
            } else {
                servicesMap = map;
            }

            log.info(" ==== serviceMap ====" + objectMapper.writeValueAsString(servicesMap));
            servicesMap.forEach((key, value) ->
            {
                Map<String, Object> valueMap = (Map<String, Object>) value;
                List<Map<String, Object>> recordsList = (List<Map<String, Object>>) valueMap.get("records");
                recordsList.forEach(records -> {

                            if (!StringUtils.isEmpty(records.get("primary_key")))
                                Collections.addAll(businessKeySet, records.get("primary_key").toString().split("~"));

                        }
                );
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(businessKeySet);

    }

    private RestTemplate getRestTemplate()
    {
        RestTemplate template = new RestTemplate();
        RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS).build();

        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectionTimeout);
        requestFactory.setHttpClient(closeableHttpClient);
        template.setRequestFactory(requestFactory);
        return template;
    }

}
