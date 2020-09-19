package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.logs.model.ErrorPayload;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;


public interface ServiceMonitoringAudit {

    @Async
    public void performAudit(ErrorPayload serviceResponse);
}
