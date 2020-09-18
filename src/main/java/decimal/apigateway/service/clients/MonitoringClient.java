package decimal.apigateway.service.clients;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MonitoringAuditServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = Constant.MONITORING_MICRO_SERVICE)
@RequestMapping(value ="vmonitoring-exe/")
public interface MonitoringClient {

    @PostMapping(value ="audit/operations", consumes = "application/json")
    void executeRequest(@RequestBody MonitoringAuditServiceRequest serviceResponse);

}
