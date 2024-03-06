package decimal.apigateway.aspects.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ApiAuthorizationConfig;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import decimal.apigateway.repository.SecApiAuthorizationConfigRepo;
import decimal.apigateway.service.rateLimiter.RateLimitService;
import lombok.extern.java.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static decimal.apigateway.commons.Constant.FAILURE_STATUS;

@Component
@Aspect
@Log
public class RateLimiterAspect{

    @Autowired
    RateLimitService rateLimitService;

    @Autowired
    ApplicationDefRedisConfigRepo applicationDefRepo;

    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    SecApiAuthorizationConfigRepo apiAuthorizationConfigRepo;


    @Pointcut("((within(decimal.apigateway.controller.V3.RegistrationControllerV3) && execution(public * executePlainRequest(..)))) && args(requestBody, httpHeaders,..)")
    public void rateLimiters(String requestBody, Map<String, String> httpHeaders) {
    }

   @Before("within(decimal.apigateway.controller.controllerV2.ExecutionControllerV2) && execution(public * executeRequest(..)) && args(requestBody, httpHeaders, destinationAppId, serviceName)")
   public void rateLimiterAdviceV2(JoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders, String destinationAppId, String serviceName) throws Throwable {
        log.info("inside before ExecutionControllerV2 executeRequest");
        String[] orgApp = getAppAndOrgId(httpHeaders);
        log.info(objectMapper.writeValueAsString(orgApp));

        String orgid = orgApp[0];
        String appid = orgApp[1];

        log.info("org id   " + orgApp[0]);
        log.info("app id  " + orgApp[1]);
       Optional<ApiAuthorizationConfig> bySourceOrgIdAndSourceAppIdAndDestinationAppId = apiAuthorizationConfigRepo.findBySourceOrgIdAndSourceAppIdAndDestinationAppId(orgid,appid,destinationAppId);
       Map<String,String> updatedHeader = new HashMap<>();
       updatedHeader.putAll(httpHeaders);

       log.info(objectMapper.writeValueAsString("updated Header  " + updatedHeader));

       if(bySourceOrgIdAndSourceAppIdAndDestinationAppId.isPresent()){
           updatedHeader.put(orgid,bySourceOrgIdAndSourceAppIdAndDestinationAppId.get().getDestinationOrgId());
           updatedHeader.put(appid,destinationAppId);
           log.info(objectMapper.writeValueAsString("updated Header before ratelimiting   " + updatedHeader));
           rateLimitValidator(updatedHeader,orgid,appid,serviceName);
       }

    }

    @Before("rateLimiters(requestBody, httpHeaders)")
    public void rateLimiterAdvice(JoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {

        String serviceName = httpHeaders.get("servicename");
        String[] orgApp = getAppAndOrgId(httpHeaders);
        String orgid = orgApp[0];
        String appid = orgApp[1];
        rateLimitValidator(httpHeaders,orgid,appid,serviceName);
    }


    private void rateLimitValidator(Map<String,String> httpHeaders,String  orgid,String appid,String serviceName) throws RouterException, IOException {
        Optional<ApplicationDefRedisConfig> applicationDefConfig = applicationDefRepo.findByOrgIdAndAppId(orgid, appid);
        if (applicationDefConfig.isEmpty())
            throw new RouterException(RouterResponseCode.APPLICATION_DEF_NOT_FOUND, (Exception) null,FAILURE_STATUS, "Application def not found for given orgId and appId");

        ApplicationDef applicationDef =  objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
       // String isRateLimitingRequired = applicationDef.getIsRateLimitingRequired();

       // if(isRateLimitingRequired != null && isRateLimitingRequired.equalsIgnoreCase("Y")){
            log.info("----------Executing rate limiter.....");
            rateLimitService.allowRequest(appid,serviceName,httpHeaders);
       // }
    }


    private String[] getAppAndOrgId(Map<String, String> httpHeaders){
        String clientId = httpHeaders.get("clientid");
        String orgid=null;

        if(Objects.isNull(clientId)){
            orgid = httpHeaders.get("orgid");
            String appid = httpHeaders.get("appid");

            if (Objects.nonNull(orgid) && Objects.nonNull(appid)){
                clientId = httpHeaders.get(Constant.ORG_ID) + Constant.TILD_SPLITTER + httpHeaders.get(Constant.APP_ID);
                httpHeaders.put(Constant.CLIENT_ID, clientId);
            }else {
                log.info("Client id is null");
                return null;
            }
        }

        String appid = clientId.split(Constant.TILD_SPLITTER)[1];
        orgid = clientId.split(Constant.TILD_SPLITTER)[0];
        return new String[]{orgid,appid};
    }
}
