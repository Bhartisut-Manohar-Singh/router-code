package decimal.apigateway.service.security;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.LogsWriter;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static decimal.apigateway.commons.Constant.JSON;

public class SecurityServiceEncImpl implements SecurityServiceEnc {


    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    EncryptionDecryptionService encryptionDecryptionService;



    public MicroserviceResponse encryptResponse(String body, Map<String, String> httpHeaders) {

        try {
            auditPayload=auditPayload();
            auditPayload = logsWriter.initializeLog(body, JSON,httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(body);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));

            MicroserviceResponse microserviceResponse = encryptionDecryptionService.encryptResponse(body, httpHeaders);
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
            return microserviceResponse;
        } catch (RouterException e) {
            throw new RuntimeException(e);
        }
    }


    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

    }
}
