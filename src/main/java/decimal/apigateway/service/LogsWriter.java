package decimal.apigateway.service;

import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.Payload;
import decimal.logs.model.RequestIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogsWriter
{
    private LogsConnector logsConnector = LogsConnector.newInstance();

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public void writeSystemPayload(Payload payload) {
        RequestIdentifier requestIdentifier = auditTraceFilter.requestIdentifier;

        logsConnector.system(requestIdentifier.getRequestId(), requestIdentifier.getSystemName(), new Payload(payload));
    }

    public void writeErrorPayload(ErrorPayload errorPayload)
    {
        errorPayload.setRequestIdentifier(auditTraceFilter.requestIdentifier);

        logsConnector.error(auditTraceFilter.requestIdentifier.getRequestId(), auditTraceFilter.requestIdentifier.getSystemName(), new ErrorPayload(errorPayload));
    }

    public void writeEndpointPayload(String transId, String systemName, Payload payload)
    {
        logsConnector.endpoint(transId,systemName,new Payload(payload));
    }
}
