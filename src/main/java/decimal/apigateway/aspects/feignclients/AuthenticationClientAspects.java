package decimal.apigateway.aspects.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class AuthenticationClientAspects {

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(responseEntity, httpHeaders)")
    public void feignClients(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) {
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(httpHeaders)")
    public void logout(Map<String, String> httpHeaders) {
    }

    @Around("feignClients(responseEntity, httpHeaders)")
    public MicroserviceResponse initiateEndpointForRegistration(ProceedingJoinPoint proceedingJoinPoint, ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) throws Throwable {
        ResponseEntity<Object> responseEntity1 = (ResponseEntity<Object>) proceedingJoinPoint.proceed();
        MicroserviceResponse response = objectMapper.convertValue(responseEntity1.getBody(),MicroserviceResponse.class);
        String status = response.getStatus();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status)) {
            throw new RouterException(response.getResponse());
        }
        return response;
    }

    @Around("feignClients(responseEntity, httpHeaders)")
    public MicroserviceResponse initiateEndpointForAuthentication(ProceedingJoinPoint proceedingJoinPoint, ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) throws Throwable {
        return initiateEndpointForRegistration(proceedingJoinPoint,responseEntity, httpHeaders);
    }

    @Around("logout(httpHeaders)")
    public MicroserviceResponse logoutAdvice(ProceedingJoinPoint proceedingJoinPoint, Map<String, String> httpHeaders) throws Throwable {
        return initiateEndpointForRegistration(proceedingJoinPoint, null, httpHeaders);
    }
}
