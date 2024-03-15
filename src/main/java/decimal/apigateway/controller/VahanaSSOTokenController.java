package decimal.apigateway.controller;

import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.SSOTokenModel;
import decimal.apigateway.service.authentication.VahanaSSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class VahanaSSOTokenController {

    @Autowired
    VahanaSSOService vahanaSSOService;

    @PostMapping("generate/SSOToken")
    public Object generateSSOToken(@RequestBody SSOTokenModel ssoTokenModel, @RequestHeader Map<String,String> httpHeaders) throws RouterException, IOException {
        return vahanaSSOService.generateSSOToken(ssoTokenModel,httpHeaders);
    }

    @PostMapping("validate/SSOToken")
    public Object validateSSOToken(@RequestBody SSOTokenModel ssoTokenModel,@RequestHeader Map<String,String> httpHeaders) throws IOException, RouterException {
        return vahanaSSOService.validateSSOToken(ssoTokenModel,httpHeaders);

    }

    @PostMapping("sso-login-details")
    public Object getSSOLoginDetails(@RequestBody SSOTokenModel ssoTokenModel,@RequestHeader Map<String,String> httpHeaders) throws IOException, RouterException {
        return vahanaSSOService.ssoLoginDetails(ssoTokenModel,httpHeaders);

    }
}
