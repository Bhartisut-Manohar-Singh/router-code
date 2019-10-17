package decimal.apigateway.aspects.feignclients;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class EsbAspect {

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.EsbClient.*(..)) && args(requestBody, httpHeaders)")
    public void feignClients(String requestBody, Map<String, String> httpHeaders) {
    }

    @Around("feignClients(requestBody, httpHeaders)")
    public Object initiateRequestForEsb(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
        return proceedingJoinPoint.proceed();
    }
}
