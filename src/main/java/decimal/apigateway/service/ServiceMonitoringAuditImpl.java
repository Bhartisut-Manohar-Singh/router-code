package decimal.apigateway.service;

import decimal.apigateway.model.MonitoringAuditServiceRequest;
import decimal.apigateway.model.RequestModel;
import decimal.apigateway.service.clients.MonitoringClient;
import decimal.logs.model.ErrorPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServiceMonitoringAuditImpl implements ServiceMonitoringAudit{

    @Autowired
    MonitoringClient monitoringClient;

    @Override
    public void performAudit(ErrorPayload serviceResponse) {

        RequestModel  requestModel= new RequestModel();
        requestModel.setAppId(serviceResponse.getRequestIdentifier().getAppId());
        requestModel.setOrgId(serviceResponse.getRequestIdentifier().getOrgId());
        requestModel.setLoginId(serviceResponse.getRequestIdentifier().getLoginId());
        requestModel.setServiceName(serviceResponse.getRequestIdentifier().getArn());

        if(null!=serviceResponse.getRequestIdentifier().getArn() && !serviceResponse.getRequestIdentifier().getArn().isEmpty()) {
            MonitoringAuditServiceRequest monitoringRequest = new MonitoringAuditServiceRequest();
            monitoringRequest.setRequest(requestModel);
            monitoringRequest.setType("VALIDATION");
            monitoringRequest.setMessage(serviceResponse.getSystemError().getMessage());

            monitoringClient.executeRequest(monitoringRequest);
        }

    }
}
