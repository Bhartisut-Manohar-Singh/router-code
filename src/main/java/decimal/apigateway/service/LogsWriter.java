package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.logs.connector.LogsConnector;
import decimal.logs.constant.LogsIdentifier;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.RequestIdentifier;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static decimal.apigateway.commons.Constant.MULTIPART;

@Log
@Service
@RequestScope
public class LogsWriter {

    @Autowired
    LogsConnector logsConnector;

    @Autowired
    private AuditPayload auditPayload;

    @Value("${dms.default.servicename}")
    private String dmsDefaultServiceName;

    @Value("${dynamic.router.default.servicename}")
    private String dynamicDefaultServiceName;

    @Autowired
    ObjectMapper objectMapper;


    public AuditPayload initializeLog(String request,String requestType, Map<String, String> httpHeaders)
    {
        auditPayload.setRequestTimestamp(Instant.now());

        RequestIdentifier requestIdentifier = getRequestIdentifier(httpHeaders,requestType);

        auditPayload.setRequestIdentifier(requestIdentifier);

        auditPayload.getRequest().setHeaders(httpHeaders);

        return auditPayload;
    }

    public AuditPayload initializeLog(String request,String requestType, Map<String, String> httpHeaders,Instant requestTimestamp)
    {
        auditPayload.setRequestTimestamp(requestTimestamp);

        RequestIdentifier requestIdentifier = getRequestIdentifier(httpHeaders,requestType);

        auditPayload.setRequestIdentifier(requestIdentifier);

        auditPayload.getRequest().setHeaders(httpHeaders);

        return auditPayload;
    }


    public void updateLog(AuditPayload auditPayload) throws JsonProcessingException {
        auditPayload.setResponseTimestamp(Instant.now());
        auditPayload.setTimeTaken(auditPayload.getResponseTimestamp().toEpochMilli() - auditPayload.getRequestTimestamp().toEpochMilli());

        AuditPayload auditPayloadFinal =new AuditPayload(auditPayload.getRequestTimestamp(),auditPayload.getResponseTimestamp(),auditPayload.getTimeTaken(),auditPayload.getRequest(),auditPayload.getResponse(),auditPayload.getStatus(),auditPayload.getRequestIdentifier(),auditPayload.isLogRequestAndResponse());
        logsConnector.audit(auditPayloadFinal);
    }


    private Predicate<String> isNotNullAndNotEmpty = (str) -> str != null && !str.isEmpty();

    private RequestIdentifier getRequestIdentifier(Map<String, String> requestHeaders,String requestType) {
        RequestIdentifier requestIdentifier = new RequestIdentifier();


        String clientId = requestHeaders.get(LogsIdentifier.clientid.name());
        String orgId = requestHeaders.get(LogsIdentifier.orgid.name());
        String appId = requestHeaders.get(LogsIdentifier.appid.name());
        String logOrgId = requestHeaders.get(LogsIdentifier.logorgid.name());
        String logAppId = requestHeaders.get(LogsIdentifier.logappid.name());
        String serviceName = requestHeaders.get(LogsIdentifier.servicename.name());
        String apiName = requestHeaders.get(LogsIdentifier.apiname.name());

        String requestId = requestHeaders.get(LogsIdentifier.requestid.name());
        String traceId = requestHeaders.get(LogsIdentifier.traceid.name());

        String username = requestHeaders.get(LogsIdentifier.username.name());
        String loginId = requestHeaders.get(LogsIdentifier.loginid.name());

        if (isNotNullAndNotEmpty.test(clientId)) {
            String[] split = clientId.split("~");
            orgId = split[0];
            appId = split[1];
        }

        String arn = isNotNullAndNotEmpty.test(serviceName) ? serviceName : apiName;


        if(arn.equalsIgnoreCase("undefined"))
        arn = requestType.equals(MULTIPART)?dmsDefaultServiceName:dynamicDefaultServiceName;

        String finalTraceId = isNotNullAndNotEmpty.test(traceId) ? traceId : (isNotNullAndNotEmpty.test(requestId) ? requestId : UUID.randomUUID().toString());

        if (loginId != null && !loginId.isEmpty())
            requestIdentifier.setLoginId(loginId);
        else if (username != null && !username.isEmpty()) {
            try {
                String[] split = username.split("~");
                requestIdentifier.setLoginId(split[2]);
            } catch (Exception ex) {
                log.info("Unable to get login Id");
            }
        }

        requestIdentifier.setOrgId(orgId);
        requestIdentifier.setAppId(appId);
        requestIdentifier.setLogOrgId(logOrgId);
        requestIdentifier.setLogAppId(logAppId);
        requestIdentifier.setSystemName("api-gateway");
        requestIdentifier.setArn(arn);
        requestIdentifier.setTraceId(finalTraceId);
        requestIdentifier.setSpanId(UUID.randomUUID().toString());

        return requestIdentifier;
    }



    public AuditPayload initializeLog(String request, String requestType, Map<String, String> httpHeaders, String servicaName, AuditPayload auditPayload)
    {

        auditPayload.setRequestTimestamp(Instant.now());

        RequestIdentifier requestIdentifier = getRequestIdentifier(httpHeaders,requestType, servicaName);
        auditPayload.setRequestIdentifier(requestIdentifier);

        auditPayload.getRequest().setHeaders(httpHeaders);

        return auditPayload;
    }


    private RequestIdentifier getRequestIdentifier(Map<String, String> requestHeaders,String requestType, String service) {
        RequestIdentifier requestIdentifier = new RequestIdentifier();


        String clientId = requestHeaders.get(LogsIdentifier.clientid.name());
        String orgId = requestHeaders.get(LogsIdentifier.orgid.name());
        String appId = requestHeaders.get(LogsIdentifier.appid.name());
        String logOrgId = requestHeaders.get(LogsIdentifier.logorgid.name());
        String logAppId = requestHeaders.get(LogsIdentifier.logappid.name());
        String serviceName = requestHeaders.get(LogsIdentifier.servicename.name());
        String apiName = requestHeaders.get(LogsIdentifier.apiname.name());

        String requestId = requestHeaders.get(LogsIdentifier.requestid.name());
        String traceId = requestHeaders.get(LogsIdentifier.traceid.name());

        String username = requestHeaders.get(LogsIdentifier.username.name());
        String loginId = requestHeaders.get(LogsIdentifier.loginid.name());

        if (isNotNullAndNotEmpty.test(clientId)) {
            String[] split = clientId.split("~");
            orgId = split[0];
            appId = split[1];
        }

        String arn = isNotNullAndNotEmpty.test(serviceName) ? serviceName : apiName;


        if(arn.equalsIgnoreCase("undefined"))
            arn = requestType.equals(MULTIPART)?dmsDefaultServiceName:dynamicDefaultServiceName;

        String finalTraceId = isNotNullAndNotEmpty.test(traceId) ? traceId : (isNotNullAndNotEmpty.test(requestId) ? requestId : UUID.randomUUID().toString());

        if (loginId != null && !loginId.isEmpty())
            requestIdentifier.setLoginId(loginId);
        else if (username != null && !username.isEmpty()) {
            try {
                String[] split = username.split("~");
                requestIdentifier.setLoginId(split[2]);
            } catch (Exception ex) {
                log.info("Unable to get login Id");
            }
        }

        requestIdentifier.setOrgId(orgId);
        requestIdentifier.setAppId(appId);
        requestIdentifier.setLogOrgId(logOrgId);
        requestIdentifier.setLogAppId(logAppId);
        requestIdentifier.setSystemName(service);
        requestIdentifier.setArn(arn);
        requestIdentifier.setTraceId(finalTraceId);
        requestIdentifier.setSpanId(UUID.randomUUID().toString());

        return requestIdentifier;
    }

}
