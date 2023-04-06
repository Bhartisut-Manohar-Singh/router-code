package decimal.apigateway.service.clients;


import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.MicroserviceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(value =  Constant.API_SECURITY_MICRO_SERVICE)
public interface SecurityClient
{
    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validateRegistration")
    MicroserviceResponse validateRegistration(@RequestBody String requestBody, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validateAuthentication")
    MicroserviceResponse validateAuthentication(@RequestBody String requestBody, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/v2/validateAuthentication")
    MicroserviceResponse validateAuthenticationV2(@RequestBody String requestBody, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/generateResponseHash")
    MicroserviceResponse generateResponseHash(@RequestBody String finalResponse, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/generateAuthResponseHash")
    MicroserviceResponse generateAuthResponseHash(@RequestBody String finalResponse, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/decryptRequestWithoutSession")
    MicroserviceResponse decryptRequestWithoutSession(@RequestBody String finalResponse, @RequestHeader Map<String, String> httpHeaders);
    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/encryptResponseWithoutSession")
    MicroserviceResponse encryptResponseWithoutSession(@RequestBody Object finalResponse, @RequestHeader Map<String, String> httpHeaders);
    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/encryptResponse")
    MicroserviceResponse encryptResponse(@RequestBody Object finalResponse, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/decryptRequest")
    MicroserviceResponse decryptRequest(@RequestBody String finalResponse, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validate/{validationType}")
    MicroserviceResponse validate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String validationType);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validateExecutionRequest")
    MicroserviceResponse validateExecutionRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);

    @PostMapping(value =  Constant.API_SECURITY_MICRO_SERVICE + "/validatePlainRequest/{serviceName}")
    MicroserviceResponse validatePlainRequest(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String serviceName);

    @PostMapping(value = Constant.API_SECURITY_MICRO_SERVICE + "/v2/validateExecutionRequest")
    MicroserviceResponse validateExecutionRequestV2(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders);
}