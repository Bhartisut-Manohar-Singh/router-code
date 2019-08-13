package decimal.apigateway.aspects.feignclients;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.EndpointDetails;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.LogService;
import decimal.apigateway.exception.RouterException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class AuthenticationClientAspects {
    @Autowired
    LogsData logsData;

    @Autowired
    LogService logService;

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(requestBody, httpHeaders)")
    public void feignClients(String requestBody, Map<String, String> httpHeaders){}

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.AuthenticationClient.*(..)) && args(httpHeaders)")
    public void logout(Map<String, String> httpHeaders){}

    @Around("feignClients(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForRegistration(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable
    {
        EndpointDetails endpointDetails = logService.initiateEndpoint(Constant.AUTHENTICATION_MICRO_SERVICE, requestBody, httpHeaders);

        MicroserviceResponse response = (MicroserviceResponse) proceedingJoinPoint.proceed();

        String status = response.getStatus();

        logService.updateEndpointDetails(response, status, endpointDetails);

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
        {
            endpointDetails.setOtherInfo("Error in executing request for: "+ proceedingJoinPoint.getSignature().getName() + " in " + Constant.AUTHENTICATION_MICRO_SERVICE);

            logsData.getEndpointDetails().add(endpointDetails);

            throw new RouterException(response.getResponse());
        }

        endpointDetails.setOtherInfo("Successfully executed request for " + proceedingJoinPoint.getSignature().getName() + " in "  + Constant.AUTHENTICATION_MICRO_SERVICE);

        logsData.getEndpointDetails().add(endpointDetails);

        return response;
    }

    @Around("feignClients(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForAuthentication(ProceedingJoinPoint proceedingJoinPoint, Object requestBody, Map<String, String> httpHeaders) throws Throwable
    {
        return initiateEndpointForRegistration(proceedingJoinPoint, String.valueOf(requestBody), httpHeaders);
    }

    @Around("logout(httpHeaders)")
    public MicroserviceResponse logoutAdvice(ProceedingJoinPoint proceedingJoinPoint, Map<String, String> httpHeaders) throws Throwable {
      return initiateEndpointForRegistration(proceedingJoinPoint, null, httpHeaders);
    }
}
