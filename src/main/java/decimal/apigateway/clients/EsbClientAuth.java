package decimal.apigateway.clients;

import decimal.apigateway.commons.ConstantsAuth;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = ConstantsAuth.ESB)
public interface EsbClientAuth {

    @PostMapping(value = ConstantsAuth.ESB + "/service-executor/execute", consumes = "application/json")
    ResponseEntity<Object> executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value = ConstantsAuth.ESB + "/service-executor/execute-plain", consumes = "application/json")
    ResponseEntity<Object> executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value = ConstantsAuth.ESB + "/service-executor/v2/execute", consumes = "application/json")
    ResponseEntity<Object> executeRequestV2(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  ConstantsAuth.ESB + "/authenticate", consumes = "application/json")
    Object executeAuthentication(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value = ConstantsAuth.ESB + "/multipart/execute-plain", consumes = "application/json")
    ResponseEntity<byte[]> executeMultipart(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  ConstantsAuth.ESB + "/v2/authenticate", consumes = "application/json")
    Object executev2Authentication(@RequestBody Object request, @RequestHeader Map<String, String> httpHeaders);

}
