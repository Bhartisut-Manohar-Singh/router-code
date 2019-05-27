package decimal.apigateway.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.Jackson;
import decimal.apigateway.commons.RouterOperations;
import decimal.apigateway.model.EndpointDetails;
import decimal.apigateway.model.LogsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    public void initiateLogsData(String request, Map<String, String> httpHeaders) {
        List<String> clientId = RouterOperations.getStringArray(httpHeaders.get("clientid"), Constant.TILD_SPLITTER);

        logsData.setOrgId(clientId.get(0));
        logsData.setAppId(clientId.get(1));
        logsData.setEndpointDetails(new ArrayList<>());
        logsData.setResourceName(httpHeaders.get("servicename"));
        logsData.setResourceVersion(httpHeaders.get("version"));
        logsData.setRequestTimeStamp(CURRENT_TIME_STAMP.get());
        logsData.setRequestId(httpHeaders.get("requestid"));

        logsData.setRequest(jackson.objectToObjectNode(request));
        logsData.setRequestHeaders(jackson.objectToObjectNode(httpHeaders));


        try {
            logsData.setProcessedByServer(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ERROR_LOGGER.error("Not able to find IP address of machine.", logsData.getRequestId(), e);
            logsData.setProcessedByServer("Not able to find IP address of machine.Error:" + e.getMessage());
        }
    }

    public void updateLogsData(Object response, String statusCode, String status)
    {
        logsData.setResponseTimeStamp(CURRENT_TIME_STAMP.get());
        logsData.setExecutionTimeMs(logsData.getResponseTimeStamp().getTime() - logsData.getRequestTimeStamp().getTime());
        logsData.setResponse(jackson.objectToObjectNode(response));
        logsData.setResponseCode(statusCode);
        logsData.setResponseStatus(status);

        try {
            System.out.println("Final logsData object: " + Jackson.objectToJsonString(new LogsData(logsData)));
        } catch (IOException e) {
            ERROR_LOGGER.error("Error in writing final object of logs to console");
        }
    }
}
