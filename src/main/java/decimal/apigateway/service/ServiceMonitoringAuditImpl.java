package decimal.apigateway.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.service.clients.MonitoringClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServiceMonitoringAuditImpl implements ServiceMonitoringAudit{

    @Autowired
    MonitoringClient monitoringClient;

    @Override
    public void performAudit(Map<String, String> httpHeaders, Object serviceResponse) {
      monitoringClient.executeRequest(serviceResponse,httpHeaders);
    }
}
