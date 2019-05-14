package decimal.apigateway.service.clients;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value =  Constant.API_SECURITY_MICRO_SERVICE)
public interface SecurityClient
{
    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validateRegistration")
    MicroserviceResponse validateRegistration(@RequestBody String requestBody, @RequestHeader Map<String, String> httpHeaders);
}
