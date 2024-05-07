package decimal.apigateway.aspects.feignclients;


import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.exception.ConfigFetchException;
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

@Component
@Aspect
@Log
public class DeleteCacheAspect {

    @Autowired
    ApplicationDefRedisConfigRepo applicationDefConfigRepo;

    @Autowired
    ObjectMapper objectMapper;

    private void verifyClientSecret(String appId, String orgId , String clientSecret) {
      /*  String clientSecret = httpHeaders.get(Constant.CLIENT_SECRET);
        log.info(" client Secret from headers: {}" + clientSecret);
        String appId = ssoTokenModel.getAppId();
*/
        log.info("appid----------"+appId);

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


    @Before(value = "execution(* decimal.apigateway.controller.DeleteCacheController.*(..)) && args(appId, orgId , clientSecret)")
    public void generateSSOToken(@RequestHeader String appId, @RequestHeader String orgId, @RequestHeader String clientSecret) throws ParseException {
        log.info("in Aspect method()");
        this.verifyClientSecret(appId,orgId, clientSecret);
    }


}
