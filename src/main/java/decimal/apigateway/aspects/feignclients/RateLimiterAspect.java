/*
package decimal.apigateway.aspects.feignclients;



import decimal.apigateway.commons.Constant;
import decimal.ratelimiter.dto.ValidatorDTO;
import decimal.ratelimiter.util.RateLimitValidator;
import lombok.extern.java.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Component
@Aspect
@Log
public class RateLimiterAspect {
    @Autowired
    private RateLimitValidator rateLimitValidator;

    @Pointcut(
            "within(decimal.apigateway.controller..*)"
                    + "&& execution(public * * (..)) && args(requestBody, httpHeaders,..)")
    public void rateLimiters(String requestBody, Map<String, String> httpHeaders) {
    }

    @Before("rateLimiters(requestBody, httpHeaders)")
    public void rateLimiterAdvice(JoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
        log.info("Executing rate limiter.....");

        String serviceName = httpHeaders.get("servicename");
        String clientId = httpHeaders.get("clientid");

        if(Objects.isNull(clientId)){
            String orgid = httpHeaders.get("orgid");
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
        String sourceIp = httpHeaders.get(Constant.ROUTER_HEADER_SOURCE_IP.toLowerCase());


        ValidatorDTO appValidatiorDto = new ValidatorDTO("APPLICATION", appId, appId);
        ValidatorDTO ipValidatorDto = new ValidatorDTO("IP_ADDRESS", sourceIp, appId);
        ValidatorDTO serviceValidatorDto = new ValidatorDTO("SERVICE", serviceName, appId);

        rateLimitValidator.validateRateLimit(Arrays.asList(appValidatiorDto, ipValidatorDto, serviceValidatorDto));
    }
}

*/
