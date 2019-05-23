package decimal.apigateway.service.clients;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.RouterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = Constant.AUTHENTICATION_MICRO_SERVICE)
public interface AuthenticationClient {

    @PostMapping(value = Constant.AUTHENTICATION_MICRO_SERVICE + "/register")
    MicroserviceResponse register(@RequestBody String requestBody, @RequestHeader Map<String, String> headers);

    @PostMapping(value = Constant.AUTHENTICATION_MICRO_SERVICE + "/authenticate")
    MicroserviceResponse authenticate(@RequestBody Object plainRequest, @RequestHeader  Map<String, String> httpHeaders);

    @GetMapping(value = Constant.AUTHENTICATION_MICRO_SERVICE + "/logout")
    MicroserviceResponse logout(@RequestHeader  Map<String, String> httpHeaders);

    @GetMapping(value = Constant.AUTHENTICATION_MICRO_SERVICE + "/forceLogout")
    RouterResponse forceLogout(Map<String, String> httpHeaders);

}
