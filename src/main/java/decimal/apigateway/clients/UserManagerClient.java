package decimal.apigateway.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USERMANAGEMENT")
public interface UserManagerClient {

    @PostMapping(value = "/user/v2/management/login-user-details")
    public Object loginDetails(@RequestParam String email );
}
