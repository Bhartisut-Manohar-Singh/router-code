package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.model.MonitoringAuditServiceRequest;
import decimal.apigateway.model.RequestModel;
import decimal.apigateway.service.clients.MonitoringClient;
import decimal.logs.model.ErrorPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log
@Service
public class ServiceMonitoringAuditImpl implements ServiceMonitoringAudit{

    @Autowired
    MonitoringClient monitoringClient;

    @Override
    public void performAudit(ErrorPayload serviceResponse){

        RequestModel  requestModel= new RequestModel();
        requestModel.setAppId(serviceResponse.getRequestIdentifier().getAppId());
        requestModel.setOrgId(serviceResponse.getRequestIdentifier().getOrgId());
        requestModel.setLoginId(serviceResponse.getRequestIdentifier().getLoginId());
        requestModel.setServiceName(serviceResponse.getRequestIdentifier().getArn());
        requestModel.setRequestId(serviceResponse.getRequestIdentifier().getTraceId());

        if(null!=serviceResponse.getRequestIdentifier().getArn() && !serviceResponse.getRequestIdentifier().getArn().isEmpty()) {
            MonitoringAuditServiceRequest monitoringRequest = new MonitoringAuditServiceRequest();
            monitoringRequest.setRequest(requestModel);
            monitoringRequest.setType("VALIDATION");
            monitoringRequest.setMessage(serviceResponse.getSystemError().getMessage());

            log.info("Calling Monitoring ================");
            log.info("MONITOrING---REQUEST");
            try {
                log.info(new ObjectMapper().writeValueAsString(monitoringRequest));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            monitoringClient.executeRequest(monitoringRequest);
        }

    }
}
