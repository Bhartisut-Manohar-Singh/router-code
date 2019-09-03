package decimal.apigateway.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.Jackson;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.model.EndpointDetails;
import decimal.apigateway.model.LogsData;
import decimal.logs.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;

@Service
public class LogService
{
    private static final Supplier<Timestamp> CURRENT_TIME_STAMP = () -> new Timestamp(System.currentTimeMillis());

    @Autowired
    LogsData logsData;

    @Autowired
    Jackson jackson;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    Payload payload;

    public EndpointDetails initiateEndpoint(String type, String request, Map<String, String> httpHeaders)
    {
        EndpointDetails endpointDetails = new EndpointDetails();

        endpointDetails.setRequestTime(CURRENT_TIME_STAMP.get());
        endpointDetails.setType(type);

        endpointDetails.setHeaders(jackson.objectToObjectNode(httpHeaders));
        endpointDetails.setRequest(jackson.objectToObjectNode(request));

        return endpointDetails;
    }

    public void updateEndpointDetails(Object response, String status, EndpointDetails endpointDetails)
    {
        endpointDetails.setResponseTime(CURRENT_TIME_STAMP.get());
        endpointDetails.setExecutionTimeMs(String.valueOf(endpointDetails.getResponseTime().getTime() - endpointDetails.getRequestTime().getTime()));
        endpointDetails.setResponse(jackson.objectToObjectNode(response));
        endpointDetails.setResponseStatus(status);
    }

    public Payload initEndpoint(String type,String request, Map<String,String> httpHeaders)
    {
        RequestPayload requestPayload=new RequestPayload();
        requestPayload.setHeaders(jackson.objectToObjectNode(request).toString());
        requestPayload.setRequest(request);
        requestPayload.setRequestTimestamp(CURRENT_TIME_STAMP.get());
        payload.setRequestPayload(requestPayload);
        return payload;
    }

    public void updateEndpoint(Object response, String status, Payload payload1)
    {
    ResponsePayload responsePayload=new ResponsePayload();
    responsePayload.setResponse(response.toString());
    responsePayload.setResponseTimestamp(CURRENT_TIME_STAMP.get());
    responsePayload.setResponseCode(responsePayload.getResponseCode());
    responsePayload.setResponseHeaders(jackson.objectToObjectNode(response).toString());
    payload.setResponsePayload(responsePayload);
    payload.setAuditPayload(new AuditPayload(auditPayload));
    logsWriter.writeEndpointPayload(auditPayload.getTransId(),auditPayload.getSystemName(),payload);
    }

    public void initiateLogsData(String request, Map<String, String> httpHeaders) {
        List<String> clientId;
        try {

            clientId = RouterOperations.getStringArray(httpHeaders.get("clientid"), Constant.TILD_SPLITTER);
        }
        catch (Exception ex)
        {
            clientId = new ArrayList<>();
            clientId.add(httpHeaders.get("orgid"));
            clientId.add(httpHeaders.get("appid"));
        }

        RequestPayload requestPayload = new RequestPayload();

        auditPayload.setOrgId(clientId.get(0));
        auditPayload.setAppId(clientId.get(1));
        auditPayload.setRequestTimestamp(new Date());
        auditPayload.setArn(httpHeaders.get("servicename"));
        auditPayload.setLogType(LogType.BUSINESS);
        auditPayload.setTransId(httpHeaders.get("requestid"));
        auditPayload.setSystemName("API_GATEWAY");

        requestPayload.setRequest(request);
        requestPayload.setHeaders(jackson.objectToString(httpHeaders));
        requestPayload.setRequestTimestamp(new Date());

        try {
            logsData.setProcessedByServer(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ERROR_LOGGER.error("Not able to find IP address of machine.", logsData.getRequestId(), e);
            logsData.setProcessedByServer("Not able to find IP address of machine.Error:" + e.getMessage());
        }

        payload.setRequestPayload(requestPayload);
    }

    public void updateLogsData(Object response, String statusCode, String status)
    {

        auditPayload.setStatus(status);
        auditPayload.setMessage("Data has been updated successfully");
        auditPayload.setResponseTimestamp(new Date());

        ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setResponse(jackson.objectToString(response));
        responsePayload.setResponseCode(statusCode);
        responsePayload.setResponseMessage(statusCode);

        payload.setResponsePayload(responsePayload);
        payload.setAuditPayload(new AuditPayload(auditPayload));

        logsWriter.writeAuditPayload(auditPayload, auditPayload.getTransId(), auditPayload.getSystemName());
        logsWriter.writeSystemPayload(payload, auditPayload.getTransId(), auditPayload.getSystemName());
    }

    public void updateErrorObject(ErrorPayload errorPayload) {
        errorPayload.setAuditPayload(new AuditPayload(auditPayload));
        logsWriter.writeErrorPayload(new ErrorPayload(errorPayload), auditPayload.getTransId(), auditPayload.getSystemName());
    }
}
