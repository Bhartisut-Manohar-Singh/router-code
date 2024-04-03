package decimal.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.EsbOutput;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.validator.PublicJwtTokenValidator;
import decimal.apigateway.service.validator.RequestValidatorV1;
import decimal.apigateway.clients.EsbClientAuth;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static decimal.apigateway.commons.Constant.*;
import static decimal.apigateway.service.ExecutionServiceImpl.getBusinessKey;
import static decimal.apigateway.service.ExecutionServiceImpl.setStatusCodeIfPresent;

@Service
@Log
public class ExecutionServiceV3Impl implements ExecutionServiceV3 {

    @Autowired
    RequestValidatorV1 requestValidator;

    @Autowired
    EsbClientAuth esbClientAuth;

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

    @Autowired
    SecurityService securityService;

    @Autowired
    PublicJwtTokenValidator publicJwtTokenValidator;


    @Override
    public Object executePlainRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("==== inside executePlainRequest ==== ");
        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        String clientId = httpHeaders.get(Constant.ORG_ID) + Constant.TILD_SPLITTER + httpHeaders.get(Constant.APP_ID);
        httpHeaders.put(Constant.CLIENT_ID, clientId);
        httpHeaders.put(Constant.ROUTER_HEADER_SECURITY_VERSION, "2");

        publicJwtTokenValidator.validate(request,httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidator.validatePlainRequest(request, httpHeaders,httpHeaders.get("servicename"));

        log.info("Public request validated successfully.... ");

        JsonNode responseNode =  objectMapper.convertValue(microserviceResponse.getResponse(),JsonNode.class);
        log.info("----- Response Node: " + objectMapper.writeValueAsString(responseNode));
        Map<String,String> headers = objectMapper.convertValue(responseNode.get("response").get("headers"), new TypeReference<>(){});
        String isDigitallySigned = headers.get(IS_DIGITALLY_SIGNED);
        String isPayloadEncrypted = headers.get(IS_PAYLOAD_ENCRYPTED);

        if (("Y").equalsIgnoreCase(isPayloadEncrypted) || ("Y").equalsIgnoreCase(isDigitallySigned))
        {
            if (! httpHeaders.containsKey(ROUTER_HEADER_SECURITY_VERSION))
                httpHeaders.put(ROUTER_HEADER_SECURITY_VERSION,"2");

            JsonNode node = objectMapper.readValue(request, JsonNode.class);

            if(("Y").equalsIgnoreCase(isPayloadEncrypted) && (!node.hasNonNull("request")))
                throw new RouterException(INVALID_REQUEST_500,"Please send a valid request",objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send encrypted payload.\"}"));

            if(("Y").equalsIgnoreCase(isPayloadEncrypted) && !httpHeaders.containsKey(Headers.txnkey.name()))
                throw new RouterException(INVALID_REQUEST_500,"Please send a valid request",objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send txnkey in HeadersV1 as your request payload is encrypted.\"}"));

            if(("Y").equalsIgnoreCase(isDigitallySigned) && !httpHeaders.containsKey(Headers.hash.name()))
                throw new RouterException(INVALID_REQUEST_500,"Please send a valid request",objectMapper.readTree("{\"status\" : \"FAILURE\",\"statusCode\" : \"INVALID_REQUEST_400\",\"message\" :\"Please send hash in HeadersV1 as your request is digitally signed.\"}"));

            httpHeaders.put(IS_DIGITALLY_SIGNED,isDigitallySigned);
            httpHeaders.put(IS_PAYLOAD_ENCRYPTED,isPayloadEncrypted);
            httpHeaders.put(Headers.clientsecret.name(), headers.get(Headers.clientsecret.name()));

            MicroserviceResponse decryptedResponse = securityService.decryptRequestWithoutSession(("Y").equalsIgnoreCase(isPayloadEncrypted) ? node.get("request").asText() : request, httpHeaders);

            if(("Y").equalsIgnoreCase(isPayloadEncrypted)) {
                request = decryptedResponse.getResponse().toString();
            }
        }

        String logsRequired = headers.get("logsrequired");
        String serviceLog = headers.get("serviceLog");
        String keysToMask = headers.get("keys_to_mask");
        String logPurgeDays =  headers.get("logpurgedays");

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
        ResponseEntity<Object> responseEntity = esbClientAuth.executePlainRequest(request, httpHeaders);

         Object responseBody = responseEntity.getBody();

         String statusCode="";
        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        if(responseHeaders!=null && responseHeaders.containsKey("statuscode"))
            statusCode=responseHeaders.get("statuscode").get(0);

        log.info(" ===== response Body from esb ===== " + new Gson().toJson(responseBody));
        List<String> businessKeySet = getBusinessKey(responseBody);
        auditPayload.getResponse().setResponse(new Gson().toJson(responseEntity.getBody()));
        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now());

        logsWriter.updateLog(auditPayload);

        if (("Y").equalsIgnoreCase(isPayloadEncrypted)) {
            MicroserviceResponse encryptedResponse = securityService.encryptResponseWithoutSession(responseEntity,
                    httpHeaders);
            Map<String, String> finalResponseMap = new HashMap<>();
            finalResponseMap.put("response", encryptedResponse.getMessage());
            EsbOutput esbOutput= new EsbOutput();
            esbOutput.setResponse(finalResponseMap);
            setStatusCodeIfPresent(statusCode,esbOutput);
            return esbOutput;
        }

        EsbOutput esbOutput= new EsbOutput();
        esbOutput.setResponse(responseBody);
        setStatusCodeIfPresent(statusCode,esbOutput);
        return esbOutput;
    }

    @Override
    public Object executeMultiPart(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);

        httpHeaders.put(Constant.ROUTER_HEADER_SECURITY_VERSION, "2");

        publicJwtTokenValidator.validate(request,httpHeaders);

        MicroserviceResponse microserviceResponse = requestValidator.validatePlainRequest(request, httpHeaders,httpHeaders.get("servicename"));

        JsonNode responseNode =  objectMapper.convertValue(microserviceResponse.getResponse(),JsonNode.class);
        log.info("----- Response Node: " + objectMapper.writeValueAsString(responseNode));
        Map<String,String> headers = objectMapper.convertValue(responseNode.get("response").get("headers"), new TypeReference<>(){});
        String logsRequired = headers.get("logsrequired");
        String serviceLog = headers.get("serviceLog");
        String keysToMask = headers.get("keys_to_mask");
        String logPurgeDays =  headers.get("logpurgedays");

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

        httpHeaders.remove("content-length");
        ResponseEntity<byte[]> responseEntity = esbClientAuth.executeMultipart(request, httpHeaders);

        Object responseBody = responseEntity.getBody();

        HttpHeaders responseHeaders = responseEntity.getHeaders();
        if(responseHeaders!=null && responseHeaders.containsKey("status"))
            auditPayload.setStatus(responseHeaders.get("status").get(0));

        Map<String,String> responseMap = new HashMap<>();
        responseMap.put("response","Response is Multipart");
        List<String> businessKeySet = getBusinessKey(responseBody);
        auditPayload.getResponse().setResponse(responseBody instanceof  byte [] ? objectMapper.writeValueAsString(responseMap) : objectMapper.writeValueAsString(responseBody));
        auditPayload.getRequestIdentifier().setBusinessFilter( businessKeySet);
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.OK.value()));
        auditPayload.getResponse().setTimestamp(LocalDateTime.now());

        logsWriter.updateLog(auditPayload);


        return responseBody;
    }


    private static Map<String, String> setHeaders(Map<String, String> httpHeaders, Map<String, String> headers, String logsRequired, String serviceLog, String logPurgeDays) {
        httpHeaders.put("logsrequired", logsRequired);
        httpHeaders.put("serviceLogs", serviceLog);
        httpHeaders.put("loginid", headers.getOrDefault("loginid","vahana"));
        httpHeaders.put("logpurgedays", logPurgeDays);
        httpHeaders.put("keys_to_mask", headers.get("keys_to_mask"));
        httpHeaders.put("executionsource","API-GATEWAY");

        return httpHeaders;
    }

}
