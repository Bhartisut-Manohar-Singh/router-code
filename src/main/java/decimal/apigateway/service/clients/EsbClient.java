package decimal.apigateway.service.clients;

import decimal.apigateway.commons.Constant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = Constant.ESB)
public interface EsbClient {

    @PostMapping(value = Constant.ESB + "/service-executor/execute", consumes = "application/json")
    ResponseEntity<Object> executeRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value = Constant.ESB + "/service-executor/execute-plain", consumes = "application/json")
    ResponseEntity<Object> executePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);


    @PostMapping(value = Constant.ESB + "/multipart/execute-plain", consumes = "application/json")
    ResponseEntity<byte[]> executeMultipart(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);
  
    @PostMapping(value = Constant.ESB + "/service-executor/v2/execute", consumes = "application/json")
    ResponseEntity<Object> executeRequestV2(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

}
