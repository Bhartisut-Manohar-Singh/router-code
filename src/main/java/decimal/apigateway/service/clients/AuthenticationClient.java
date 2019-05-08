package decimal.apigateway.service.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("authentication-service")
public interface AuthenticationClient {

    @GetMapping("authentication-service/authenticate")
    String authenticate();

    @GetMapping("authentication-service/authenticate-failure")
    Object authenticateService();
}
