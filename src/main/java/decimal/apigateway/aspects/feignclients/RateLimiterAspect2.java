package decimal.apigateway.aspects.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import decimal.apigateway.service.rateLimiter.RateLimitService;
import lombok.extern.java.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Aspect
@Log
public class RateLimiterAspect2 {

    @Autowired
    RateLimitService rateLimitService;

    @Autowired
    ApplicationDefRedisConfigRepo applicationDefRepo;

    @Autowired
    ObjectMapper objectMapper;


    @Pointcut(
            "within(decimal.apigateway.controller..*)"
                    + "&& execution(public * * (..)) && args(requestBody, httpHeaders,..)")
    public void rateLimiters(String requestBody, Map<String, String> httpHeaders) {
    }

    @Before("rateLimiters(requestBody, httpHeaders)")
    public void rateLimiterAdvice(JoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
        log.info("Executing rate limiter. 2   ....");

        String serviceName = httpHeaders.get("servicename");
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
                return;
            }
        }

        String appId = clientId.split(Constant.TILD_SPLITTER)[1];

        Optional<ApplicationDefRedisConfig> applicationDefConfig = applicationDefRepo.findByOrgIdAndAppId(orgid, appId);
        ApplicationDef applicationDef =  objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
        if(applicationDef.getIsRateLimitingRequired().equalsIgnoreCase("Y")){
            rateLimitService.allowRequest(appId,serviceName,httpHeaders);
        }

    }
}
