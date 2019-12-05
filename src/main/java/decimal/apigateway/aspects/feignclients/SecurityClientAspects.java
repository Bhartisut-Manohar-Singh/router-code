package decimal.apigateway.aspects.feignclients;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class SecurityClientAspects {

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.SecurityClient.*(..))  && args(requestBody, httpHeaders,..)")
    public void feignClients1(String requestBody, Map<String, String> httpHeaders) {
    }

    @Around("feignClients1(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForSecurity(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
        MicroserviceResponse response = (MicroserviceResponse) proceedingJoinPoint.proceed();

        String status = response.getStatus();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status)) {
            throw new RouterException(response.getResponse());
        }

        return response;
    }
}
