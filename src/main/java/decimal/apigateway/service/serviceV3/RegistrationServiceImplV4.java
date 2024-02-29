package decimal.apigateway.service.serviceV3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ResponseOperations;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.AuthenticationService;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.RegistrationService;
import decimal.apigateway.service.SecurityService;
import decimal.apigateway.service.authentication.AuthenticationProcessor;
import decimal.apigateway.service.authentication.KeysGenerator;
import decimal.apigateway.service.authentication.sessionmgmt.MultipleSession;
import decimal.apigateway.service.security.EncryptionDecryptionServiceImpl;
import decimal.apigateway.service.security.SecurityServiceEnc;
import decimal.apigateway.service.validator.RequestValidatorV1;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.EventRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static decimal.apigateway.commons.Constant.*;

@Service
@CrossOrigin
@Log
public class RegistrationServiceImplV4 implements RegistrationServiceV4 {

    private ObjectMapper objectMapper;

    @Autowired
    ResponseOperations responseOperations;

    @Autowired
    RequestValidatorV1 requestValidator;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    SecurityServiceEnc securityServiceEnc;

    @Autowired
    AuditPayload auditPayload;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    @Autowired
    EncryptionDecryptionServiceImpl encryptionDecryptionService;

    @Autowired
    SecurityService securityService;

    @Autowired
    AuthenticationProcessor authenticationProcessor;

    AuthenticationService authenticationService;

    @Autowired
    public RegistrationServiceImplV4(AuthenticationService authenticationService, ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        log.info(" ==== entering register with request === " + request);
        auditPayload = logsWriter.initializeLog(request,JSON, httpHeaders);

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled);

        ObjectNode jsonNodes = objectMapper.convertValue(requestValidator.validateRegistrationRequest(request, httpHeaders), ObjectNode.class);

        String userName = jsonNodes.get("username").asText();

        httpHeaders.put("username", userName);

        auditPayload.getRequest().setRequestBody(request);

        httpHeaders.put("executionsource","API-GATEWAY");

        log.info(" === calling authenticationClient === ");
        ResponseEntity<Object> responseEntity = authenticationService.register(request, httpHeaders);

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

        log.info(" === calling securityClient === ");
        MicroserviceResponse responseHash = securityService.generateResponseHash(finalResponse, httpHeaders);

        response.addHeader("hash", responseHash.getMessage());
        node.put("hash", responseHash.getMessage());

        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));

        logsWriter.updateLog(auditPayload);
        log.info(" ==== exiting register with response === " + finalResponse);
        return finalResponse;
    }


    @Override
    public Object  authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {
        log.info("Inside authenticate method.");

        auditPayload = logsWriter.initializeLog(request,JSON, httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidator.validateAuthentication(request, httpHeaders);
        log.info("MicroserviceResponse : "+ objectMapper.writeValueAsString(microserviceResponse));

        httpHeaders.put("username", microserviceResponse.getMessage());

        Map<String, String> customData = microserviceResponse.getCustomData();

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

        ResponseEntity<Object> responseEntity = authenticate(plainRequest, httpHeaders);
        log.info("Authenticate response : "+ objectMapper.writeValueAsString(responseEntity));

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        MicroserviceResponse authenticateResponse = objectMapper.convertValue(responseEntity.getBody(),MicroserviceResponse.class);
        Map<String, Object> authResponse = objectMapper.convertValue(authenticateResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        Object finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(authResponse));
        log.info("FinalResponse : "+ objectMapper.writeValueAsString(finalResponse));

         String maskedResponse = JsonMasker.maskMessage(finalResponse.toString(), maskKeys);
         auditPayload.getResponse().setResponse(maskedResponse);

         MicroserviceResponse encryptedResponse = securityServiceEnc.encryptResponse(finalResponse.toString(), httpHeaders);

        if (!encryptedResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS_STATUS)) {
            auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            logsWriter.updateLog(auditPayload);
            return new ResponseEntity<>(authenticateResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        ObjectNode node = objectMapper.createObjectNode();

        node.put("Authorization", authResponse.get("jwtToken").toString());
        response.addHeader("Authorization", authResponse.get("jwtToken").toString());

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        MicroserviceResponse authResponseHash = securityService.generateAuthResponseHash(finalResponse.toString(), httpHeaders);
        response.addHeader("hash", authResponseHash.getMessage());
        log.info("MicroserviceResponse authResponseHash : "+ objectMapper.writeValueAsString(authResponseHash));

        node.put("hash", authResponseHash.getMessage());

        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));

        logsWriter.updateLog(auditPayload);
        log.info("FinalResponseMap : "+ objectMapper.writeValueAsString(finalResponseMap));

        return finalResponseMap;
    }

    public ResponseEntity<Object> authenticate(Object plainRequest, Map<String, String> httpHeaders) throws RouterException, IOException {

        MicroserviceResponse microserviceResponse = authenticationProcessor.authenticateV4(String.valueOf(plainRequest), httpHeaders);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", microserviceResponse.getStatus());
        return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);

    }

}
