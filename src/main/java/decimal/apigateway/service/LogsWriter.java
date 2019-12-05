package decimal.apigateway.service;

import decimal.logs.connector.LogsConnector;
import decimal.logs.constant.LogsIdentifier;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.RequestIdentifier;
import decimal.logs.model.Response;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class LogsWriter {
    public AuditPayload initializeLog(String request, Map<String, String> httpHeaders)
    {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequestTimestamp(Instant.now());

        RequestIdentifier requestIdentifier = getRequestIdentifier(httpHeaders);

        auditPayload.setRequestIdentifier(requestIdentifier);

        Request requestObj = new Request();
        requestObj.setHeaders(httpHeaders);

        auditPayload.setRequest(requestObj);

        Response response = new Response();
        auditPayload.setResponse(response);

        return auditPayload;
    }

    public void updateLog(AuditPayload auditPayload){

        auditPayload.setResponseTimestamp(Instant.now());
        auditPayload.setTimeTaken(auditPayload.getResponseTimestamp().toEpochMilli() - auditPayload.getRequestTimestamp().toEpochMilli());

        LogsConnector.newInstance().audit(auditPayload);
    }

    private Predicate<String> isNotNullAndNotEmpty = (str) -> str != null && !str.isEmpty();

    private RequestIdentifier getRequestIdentifier(Map<String, String> requestHeaders) {
        RequestIdentifier requestIdentifier = new RequestIdentifier();


        String clientId = requestHeaders.get(LogsIdentifier.clientid.name());
        String orgId = requestHeaders.get(LogsIdentifier.orgid.name());
        String appId = requestHeaders.get(LogsIdentifier.appid.name());
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

        String finalTraceId = isNotNullAndNotEmpty.test(traceId) ? traceId : (isNotNullAndNotEmpty.test(requestId) ? requestId : UUID.randomUUID().toString());

        if (loginId != null && !loginId.isEmpty())
            requestIdentifier.setLoginId(loginId);
        else if (username != null && !username.isEmpty()) {
            try {
                String[] split = username.split("~");
                requestIdentifier.setLoginId(split[2]);
            } catch (Exception ex) {
                System.out.println("Unable to get login Id");
            }
        }

        requestIdentifier.setOrgId(orgId);
        requestIdentifier.setAppId(appId);
        requestIdentifier.setSystemName("api-gateway");
        requestIdentifier.setArn(arn);
        requestIdentifier.setTraceId(finalTraceId);
        requestIdentifier.setSpanId(UUID.randomUUID().toString());

        return requestIdentifier;
    }
}
