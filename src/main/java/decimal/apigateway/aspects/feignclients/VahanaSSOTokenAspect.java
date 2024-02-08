package decimal.apigateway.aspects.feignclients;


import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.exception.ConfigFetchException;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.SSOTokenModel;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import lombok.extern.java.Log;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

import static decimal.apigateway.enums.RequestValidationTypes.CLIENT_SECRET;

@Component
@Aspect
@Log
public class VahanaSSOTokenAspect {


    @Autowired
    ApplicationDefRedisConfigRepo applicationDefConfigRepo;

    @Autowired
    ObjectMapper objectMapper;


    private void verifyClientSecret(SSOTokenModel ssoTokenModel, Map<String, String> httpHeaders) {
        String clientSecret = httpHeaders.get(Constant.CLIENT_SECRET);
        log.info(" client Secret from headers: {}" + clientSecret);
        String appId = ssoTokenModel.getAppId();

        Optional<ApplicationDefRedisConfig> applicationDefConfig = applicationDefConfigRepo.findByAppId(appId);

        if (applicationDefConfig.isEmpty()) {
            throw new ConfigFetchException(Constant.FAILURE_STATUS, " No details found for given app-id in APP_DEF ");
        }

        ApplicationDef applicationDef;
        try {
            applicationDef = objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
        } catch (IOException e) {
            throw new ConfigFetchException(Constant.FAILURE_STATUS, "No valid configuration found for given orgId and appId ");
        }

        if (!applicationDef.getClientSecret().equalsIgnoreCase(clientSecret))
            throw new ConfigFetchException(Constant.FAILURE_STATUS, "Invalid client secret");
    }


    @Before(value = "execution(* decimal.apigateway.controller.VahanaSSOTokenController.*(..)) && args(ssoTokenModel , httpHeaders)")
    public void generateSSOToken(@RequestBody SSOTokenModel ssoTokenModel, @RequestHeader Map<String, String> httpHeaders) throws ParseException {
        log.info("in Aspect method()");
        this.verifyClientSecret(ssoTokenModel, httpHeaders);
    }

    @Before(value = "execution(* decimal.apigateway.controller.VahanaSSOTokenController.*(..)) && args(ssoTokenModel , httpHeaders)")
    public void validateSSOToken(@RequestBody SSOTokenModel ssoTokenModel, @RequestHeader Map<String, String> httpHeaders) throws ParseException {

        this.verifyClientSecret(ssoTokenModel, httpHeaders);
    }

    @Before(value = "execution(* decimal.apigateway.controller.VahanaSSOTokenController.*(..)) && args(ssoTokenModel , httpHeaders)")
    public void getSSOLoginDetails(@RequestBody SSOTokenModel ssoTokenModel, @RequestHeader Map<String, String> httpHeaders) throws ParseException {

        this.verifyClientSecret(ssoTokenModel, httpHeaders);
    }
}

