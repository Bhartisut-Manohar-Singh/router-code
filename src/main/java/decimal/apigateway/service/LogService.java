package decimal.apigateway.service;

import decimal.apigateway.commons.Jackson;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.Payload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    Jackson jackson;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public Payload initEndpoint(String type, String request, Map<String, String> httpHeaders) {
        Payload payload = new Payload();

        Request requestBody = new Request();
        requestBody.setHeaders(httpHeaders);
        requestBody.setRequestBody(request);
        requestBody.setTimestamp(Instant.now());

        payload.setRequestIdentifier(auditTraceFilter.requestIdentifier);
        payload.setRequest(requestBody);

        return payload;
    }

    public void updateEndpoint(Object response, String status, Payload payload) {
        Response responsePayload = new Response();
        responsePayload.setResponse(response.toString());
        responsePayload.setTimestamp(Instant.now());
        responsePayload.setStatusCode(responsePayload.getStatusCode());
        responsePayload.setStatus(status);

        payload.setTimeTaken(responsePayload.getTimestamp().toEpochMilli() -payload.getRequest().getTimestamp().toEpochMilli());

        payload.setResponse(responsePayload);

        logsWriter.writeEndpointPayload(auditTraceFilter.requestIdentifier.getTraceId(), auditTraceFilter.requestIdentifier.getSystemName(), payload);
    }
}
