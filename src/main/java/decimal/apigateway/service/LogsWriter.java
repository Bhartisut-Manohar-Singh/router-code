package decimal.apigateway.service;

import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogsWriter {
    private LogsConnector logsConnector = LogsConnector.newInstance();

    @Autowired
    AuditTraceFilter auditTraceFilter;

    public void writeSystemPayload(Payload payload) {
        payload.setRequestIdentifier(auditTraceFilter.requestIdentifier);

        logsConnector.system(new Payload(payload));
    }

    public void writeErrorPayload(ErrorPayload errorPayload) {
        errorPayload.setRequestIdentifier(auditTraceFilter.requestIdentifier);
        logsConnector.error(new ErrorPayload(errorPayload));
    }

    public void writeEndpointPayload(String transId, String systemName, Payload payload) {
        payload.setRequestIdentifier(auditTraceFilter.requestIdentifier);

        logsConnector.endpoint(new Payload(payload));
    }
}
