package decimal.apigateway.service.executionImplV2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ResponseOperations;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.RegistrationServiceV2;
import decimal.apigateway.service.clients.AuthenticationClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidatorV2;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static decimal.apigateway.commons.Constant.JSON;


@Service
@Log
public class RegistrationServiceImplV2 implements RegistrationServiceV2 {

    private SecurityClient securityClient;

    private AuthenticationClient authenticationClient;

    private ObjectMapper objectMapper;

    @Autowired
    ResponseOperations responseOperations;

    @Autowired
    RequestValidatorV2 requestValidatorV2;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditPayload auditPayload;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    @Autowired
    public RegistrationServiceImplV2(SecurityClient securityClient, AuthenticationClient authenticationClient, ObjectMapper objectMapper) {
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        auditPayload = logsWriter.initializeLog(request,JSON, httpHeaders);

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        ObjectNode jsonNodes = objectMapper.convertValue(requestValidatorV2.validateRegistrationRequest(request, httpHeaders), ObjectNode.class);

        String userName = jsonNodes.get("username").asText();

        httpHeaders.put("username", userName);

        auditPayload.getRequest().setRequestBody(request);

        httpHeaders.put("executionsource","API-GATEWAY");

        ResponseEntity<Object> responseEntity = authenticationClient.register(request, httpHeaders);

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        MicroserviceResponse registerResponse = objectMapper.convertValue(responseEntity.getBody(),MicroserviceResponse.class);
        Map<String, Object> rsaKeysMap = objectMapper.convertValue(registerResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        auditPayload.getResponse().setResponse(objectMapper.writeValueAsString(registerResponse.getResponse()));

        String jwtToken = String.valueOf(rsaKeysMap.get("jwtToken"));

        ObjectNode node = objectMapper.createObjectNode();

        response.addHeader("Authorization", "Bearer " + jwtToken);

        node.put("Authorization", "Bearer " + jwtToken);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("rsa", objectMapper.writeValueAsString(rsaKeysMap));

        String finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(responseMap)).toString();

        MicroserviceResponse responseHash = securityClient.generateResponseHash(finalResponse, httpHeaders);

        response.addHeader("hash", responseHash.getMessage());
        node.put("hash", responseHash.getMessage());

        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));

        logsWriter.updateLog(auditPayload);
        return finalResponse;
    }

    @Override
    public Object  authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response, String destinationAppId) throws IOException, RouterException {
        log.info("Authenticate v2 called for  - " + request + " and dAppId - " + destinationAppId);
        auditPayload = logsWriter.initializeLog(request,JSON, httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidatorV2.validateAuthenticationV2(request, httpHeaders);

        log.info("Response returned by validate authentication - " + microserviceResponse);

        httpHeaders.put("username", microserviceResponse.getMessage());

        Map<String, String> customData = microserviceResponse.getCustomData();

        String destinationOrgId = "";
        if(customData != null)
        {
            String logsRequired = customData.get("appLogs");
            String serviceLog = customData.get("serviceLog");
            String logPurgeDays =  customData.get("logpurgedays");
            auditTraceFilter.setPurgeDays(logPurgeDays);
            httpHeaders.put(Constant.KEYS_TO_MASK, customData.get(Constant.KEYS_TO_MASK));
            httpHeaders.put("logsrequired", logsRequired);
            httpHeaders.put("servicelogs", serviceLog);
            httpHeaders.put("logpurgedays",logPurgeDays);
            auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));
            destinationOrgId = customData.getOrDefault("destinationOrgId", "");
        }
        else
            auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        String keysToMask = httpHeaders.get(Constant.KEYS_TO_MASK);

        List<String> maskKeys = new ArrayList<>();

        if (keysToMask != null && !keysToMask.isEmpty()) {
            String[] keysToMaskArr = keysToMask.split(",");
            maskKeys = Arrays.asList(keysToMaskArr);
        }

        Object plainRequest = microserviceResponse.getResponse();

        ObjectNode nodes = objectMapper.createObjectNode();

        String requestBody = JsonMasker.maskMessage(plainRequest.toString(), maskKeys);
        auditPayload.getRequest().setRequestBody(requestBody);

        auditPayload.getRequestIdentifier().setLoginId(microserviceResponse.getMessage().split(Constant.TILD_SPLITTER)[2]);

        httpHeaders.put("executionsource","API-GATEWAY");


        log.info("Now updating org and app id headers to destination org - " + destinationOrgId + " and dAPP id - " + destinationAppId);
        httpHeaders.put(Headers.orgid.name(), destinationOrgId);
        httpHeaders.put(Headers.appid.name(), destinationAppId);
        httpHeaders.put("destinationorgid", destinationOrgId);

        log.info("Calling authentication service.....");
        ResponseEntity<Object> responseEntity = authenticationClient.authenticateV2(plainRequest, httpHeaders);

        log.info("Response returned by authentication service -- " + responseEntity);

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        MicroserviceResponse authenticateResponse = objectMapper.convertValue(responseEntity.getBody(),MicroserviceResponse.class);

        Map<String, Object> authResponse = objectMapper.convertValue(authenticateResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        Object finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(authResponse));

        String maskedResponse = JsonMasker.maskMessage(finalResponse.toString(), maskKeys);
        auditPayload.getResponse().setResponse(maskedResponse);

        log.info("Encrypting response...");

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(finalResponse, httpHeaders);

        if (!encryptedResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS_STATUS)) {
            return new ResponseEntity<>(authenticateResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        ObjectNode node = objectMapper.createObjectNode();

        node.put("Authorization", authResponse.get("jwtToken").toString());
        response.addHeader("Authorization", authResponse.get("jwtToken").toString());



        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        log.info("Encrypting response hash.....");

        MicroserviceResponse authResponseHash = securityClient.generateAuthResponseHash(finalResponse.toString(), httpHeaders);

        response.addHeader("hash", authResponseHash.getMessage());

        node.put("hash", authResponseHash.getMessage());

        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));

        logsWriter.updateLog(auditPayload);

        log.info("Returning final response map");

        return finalResponseMap;
    }

    @Override
    public Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws RouterException {
        httpHeaders.put("executionsource","API-GATEWAY");
        ResponseEntity<Object> responseEntity = authenticationClient.forceLogout(httpHeaders);
        MicroserviceResponse microserviceResponse = objectMapper.convertValue(responseEntity.getBody(),MicroserviceResponse.class);
        return new ResponseEntity(microserviceResponse,responseEntity.getHeaders(),HttpStatus.OK);
    }

    @Override
    public Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
        try {
            MicroserviceResponse microserviceResponse = requestValidatorV2.validateLogout(request, httpHeaders);

            httpHeaders.put("username", microserviceResponse.getResponse().toString());

            auditTraceFilter.requestIdentifier.setLoginId(microserviceResponse.getResponse().toString().split(Constant.TILD_SPLITTER)[2]);

        }
        catch (Exception ex)
        {
            log.info("Security Failure!!!!!");
        }

        httpHeaders.put("executionsource","API-GATEWAY");
        ResponseEntity<Object> responseEntity = authenticationClient.logout(httpHeaders);
        MicroserviceResponse microserviceResponse = objectMapper.convertValue(responseEntity.getBody(),MicroserviceResponse.class);
        return new ResponseEntity(microserviceResponse,responseEntity.getHeaders(),HttpStatus.OK);
    }
}
