package decimal.apigateway.service.clients;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MonitoringAuditServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = Constant.MONITORING_MICRO_SERVICE)
public interface MonitoringClient {

    @PostMapping(value ="vmonitoring-exe/audit/operations", consumes = "application/json")
    void executeRequest(@RequestBody MonitoringAuditServiceRequest serviceResponse);

}
