package decimal.apigateway.service;

import decimal.logs.connector.LogsConnector;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Payload;
import org.springframework.stereotype.Service;

@Service
public class LogsWriter
{
    private LogsConnector logsConnector = LogsConnector.newInstance();

    public void writeSystemPayload(Payload payload1, String transId, String systemName) {
        logsConnector.system(transId, systemName, new Payload(payload1));
    }

    public void writeAuditPayload(AuditPayload auditPayload1, String transId, String systemName) {
       logsConnector.audit(transId, systemName, new AuditPayload(auditPayload1));
    }
}
