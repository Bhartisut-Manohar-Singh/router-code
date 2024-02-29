package decimal.apigateway.aspects.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.domain.ApplicationDefRedisConfig;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.repository.ApplicationDefRedisConfigRepo;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.rateLimiter.RateLimitService;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
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

import static decimal.apigateway.commons.Constant.FAILURE_STATUS;
import static decimal.apigateway.commons.Constant.JSON;

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

    @Pointcut("((within(decimal.apigateway.controller.controllerV2.ExecutionControllerV2) && "
            + "(execution(public * executePlainRequest(..)) || execution(public * executeRequest(..)))) "
            + "|| (within(decimal.apigateway.controller.V3.RegistrationControllerV3) && execution(public * executePlainRequest(..)))) "
            + "&& args(requestBody, httpHeaders,..)")
    public void rateLimiters(String requestBody, Map<String, String> httpHeaders) {
    }

    @Before("rateLimiters(requestBody, httpHeaders)")
    public void rateLimiterAdvice(JoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {

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
        if (applicationDefConfig.isEmpty())
            throw new RouterException(RouterResponseCode.APPLICATION_DEF_NOT_FOUND, (Exception) null,FAILURE_STATUS, "Application def not found for given orgId and appId");

        ApplicationDef applicationDef =  objectMapper.readValue(applicationDefConfig.get().getApiData(), ApplicationDef.class);
        String isRateLimitingRequired = applicationDef.getIsRateLimitingRequired();
        if(isRateLimitingRequired != null && isRateLimitingRequired.equalsIgnoreCase("Y")){
            log.info("----------Executing rate limiter.....");
            rateLimitService.allowRequest(appId,serviceName,httpHeaders);
        }

    }
}
