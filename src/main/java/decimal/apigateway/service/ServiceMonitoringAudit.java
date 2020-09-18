package decimal.apigateway.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;


public interface ServiceMonitoringAudit {

    @Async
    public void performAudit(Map<String,String> httpHeaders, Object serviceResponse);
}
