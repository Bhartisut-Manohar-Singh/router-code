package decimal.apigateway.service.executionImplV2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.ExecutionServiceV2;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.clients.EsbClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidator;
import decimal.apigateway.service.validator.RequestValidatorV2;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static decimal.apigateway.commons.Constant.JSON;
import static decimal.apigateway.commons.Constant.SUCCESS_STATUS;
import static decimal.apigateway.service.ExecutionServiceImpl.getBusinessKey;

@Service
public class ExecutionServiceImplV2 implements ExecutionServiceV2 {

    @Autowired
    RequestValidatorV2 requestValidatorV2;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EsbClient esbClient;

    @Autowired
    SecurityClient securityClient;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;

    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    /*@Value("${server.servlet.context-path}")
    String path;*/
    @Override
    public Object executeRequest(String destinationAppId,String serviceNmae, String request, Map<String, String> httpHeaders) throws IOException, RouterException {
        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);
        Map<String, String> updatedHttpHeaders =requestValidatorV2.validateRequest(request, httpHeaders,auditPayload);

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

        auditTraceFilter.setIsServicesLogsEnabled(isHttpTracingEnabled && "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog));

        JsonNode node = objectMapper.readValue(request, JsonNode.class);

        MicroserviceResponse decryptedResponse = securityClient.decryptRequest(node.get("request").asText(), httpHeaders);

        String maskRequestBody= JsonMasker.maskMessage(decryptedResponse.getResponse().toString(), maskKeys);
        auditPayload.getRequest().setRequestBody(maskRequestBody);
        updatedHttpHeaders.put("executionsource","API-GATEWAY");

        ResponseEntity<Object> responseEntity = esbClient.executeRequest(decryptedResponse.getResponse().toString(), updatedHttpHeaders);
        HttpHeaders responseHeaders = responseEntity.getHeaders();

        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        List<String> businessKeySet = getBusinessKey(responseEntity.getBody());
        String responseBody = JsonMasker.maskMessage(objectMapper.writeValueAsString(responseEntity.getBody()), maskKeys);
        auditPayload.getResponse().setResponse(responseBody);
        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);
        httpHeaders.put("executionsource","API-GATEWAY");

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
}
