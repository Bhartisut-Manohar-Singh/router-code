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
public class AuthenticationClientAspects {

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(requestBody, httpHeaders)")
    public void feignClients(String requestBody, Map<String, String> httpHeaders) {
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(httpHeaders)")
    public void logout(Map<String, String> httpHeaders) {
    }

    @Around("feignClients(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForRegistration(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
        MicroserviceResponse response = (MicroserviceResponse) proceedingJoinPoint.proceed();

        String status = response.getStatus();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status)) {
            throw new RouterException(response.getResponse());
        }
        return response;
    }

    @Around("feignClients(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForAuthentication(ProceedingJoinPoint proceedingJoinPoint, Object requestBody, Map<String, String> httpHeaders) throws Throwable {
        return initiateEndpointForRegistration(proceedingJoinPoint, String.valueOf(requestBody), httpHeaders);
    }

    @Around("logout(httpHeaders)")
    public MicroserviceResponse logoutAdvice(ProceedingJoinPoint proceedingJoinPoint, Map<String, String> httpHeaders) throws Throwable {
        return initiateEndpointForRegistration(proceedingJoinPoint, null, httpHeaders);
    }
}
