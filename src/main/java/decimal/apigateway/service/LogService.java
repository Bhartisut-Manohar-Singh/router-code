package decimal.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Jackson;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.*;
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

        payload.setResponse(responsePayload);

        logsWriter.writeEndpointPayload(auditTraceFilter.requestIdentifier.getTraceId(), auditTraceFilter.requestIdentifier.getSystemName(), payload);
    }

    @Autowired
    ObjectMapper objectMapper;

    public void createErrorPayload(Object response, String statusCode, String status) {

        ErrorPayload errorPayload = new ErrorPayload();

        BusinessError businessError = new BusinessError();

        ObjectNode jsonNodes = objectMapper.convertValue(response, ObjectNode.class);

        statusCode = jsonNodes.get("status") != null ? jsonNodes.get("status").asText() : statusCode;

        String message = jsonNodes.get("message") !=null ? jsonNodes.get("message").asText() : status;

        businessError.setDetailedError(jsonNodes.toString());
        businessError.setErrorCode(statusCode);
        businessError.setErrorMessage(message);

        errorPayload.setBusinessError(businessError);

        logsWriter.writeErrorPayload(errorPayload);
    }
}
