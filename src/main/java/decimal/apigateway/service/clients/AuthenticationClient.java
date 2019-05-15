package decimal.apigateway.service.clients;

import decimal.apigateway.commons.Constant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = Constant.AUTHENTICATION_MICRO_SERVICE)
public interface AuthenticationClient {

    @PostMapping(value = Constant.AUTHENTICATION_MICRO_SERVICE + "/register")
    Object register(@RequestBody String requestBody, @RequestHeader Map<String, String> headers);
}
