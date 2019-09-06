package decimal.apigateway.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.Jackson;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.model.EndpointDetails;
import decimal.apigateway.model.LogsData;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.Payload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;

@Service
public class LogService {
    private static final Supplier<Timestamp> CURRENT_TIME_STAMP = () -> new Timestamp(System.currentTimeMillis());

    @Autowired
    LogsData logsData;

    @Autowired
    Jackson jackson;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public EndpointDetails initiateEndpoint(String type, String request, Map<String, String> httpHeaders) {
        EndpointDetails endpointDetails = new EndpointDetails();

        endpointDetails.setRequestTime(CURRENT_TIME_STAMP.get());
        endpointDetails.setType(type);

        endpointDetails.setHeaders(jackson.objectToObjectNode(httpHeaders));
        endpointDetails.setRequest(jackson.objectToObjectNode(request));

        return endpointDetails;
    }

    public void updateEndpointDetails(Object response, String status, EndpointDetails endpointDetails) {
        endpointDetails.setResponseTime(CURRENT_TIME_STAMP.get());
        endpointDetails.setExecutionTimeMs(String.valueOf(endpointDetails.getResponseTime().getTime() - endpointDetails.getRequestTime().getTime()));
        endpointDetails.setResponse(jackson.objectToObjectNode(response));
        endpointDetails.setResponseStatus(status);
    }

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

    public void initiateLogsData(String request, Map<String, String> httpHeaders) {
        List<String> clientId;
        try {

            clientId = RouterOperations.getStringArray(httpHeaders.get("clientid"), Constant.TILD_SPLITTER);
        } catch (Exception ex) {
            clientId = new ArrayList<>();
            clientId.add(httpHeaders.get("orgid"));
            clientId.add(httpHeaders.get("appid"));
        }

        Request Request = new Request();

        try {
            logsData.setProcessedByServer(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ERROR_LOGGER.error("Not able to find IP address of machine.", logsData.getRequestId(), e);
            logsData.setProcessedByServer("Not able to find IP address of machine.Error:" + e.getMessage());
        }
    }

    public void updateLogsData(Object response, String statusCode, String status) {

    }

    public void updateErrorObject(ErrorPayload errorPayload) {

        errorPayload.setRequestIdentifier(auditTraceFilter.requestIdentifier);

        logsWriter.writeErrorPayload(errorPayload);
    }
}
