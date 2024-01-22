package decimal.apigateway.controller;


import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.security.SecurityServiceEnc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SecurityController {

    private SecurityServiceEnc securityServiceEnc;

    @Autowired
    public SecurityController(SecurityServiceEnc securityServiceEnc) {
        this.securityServiceEnc = securityServiceEnc;
    }

    @PostMapping("encryptResponse")
    MicroserviceResponse encryptResponse(@RequestBody Object finalResponse, @RequestHeader Map<String, String> httpHeaders) {

        return securityServiceEnc.encryptResponse(finalResponse.toString(),httpHeaders);

    }
}
