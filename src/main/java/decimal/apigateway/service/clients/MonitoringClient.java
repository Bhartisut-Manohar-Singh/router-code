package decimal.apigateway.service.clients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = Constant.MONITORING_MICRO_SERVICE)
public interface MonitoringClient {

    @PostMapping(value = Constant.MONITORING_MICRO_SERVICE + "/audit/operations", consumes = "application/json")
    Object executeRequest(@RequestBody Object serviceResponse, @RequestHeader Map<String, String> httpHeaders);

}
