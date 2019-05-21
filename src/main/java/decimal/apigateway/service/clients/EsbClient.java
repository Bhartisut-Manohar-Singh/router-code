package decimal.apigateway.service.clients;

import decimal.apigateway.commons.Constant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = Constant.ESB)
public interface EsbClient {

    @PostMapping(value =  Constant.ESB + "/service-executor/execute", consumes = "application/json")
    Object executeAuthentication(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);
}
