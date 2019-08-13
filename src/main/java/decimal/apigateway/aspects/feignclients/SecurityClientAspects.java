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
public class SecurityClientAspects
{
    @Autowired
    LogsData logsData;

    @Autowired
    LogService logService;


    @Pointcut(value = "execution(* decimal.apigateway.service.clients.SecurityClient.*(..))  && args(requestBody, httpHeaders,..)")
    public void feignClients1(String requestBody, Map<String, String> httpHeaders){}

    @Around("feignClients1(requestBody, httpHeaders)")
    public MicroserviceResponse initiateEndpointForSecurity(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable
    {
        EndpointDetails endpointDetails = logService.initiateEndpoint(Constant.API_SECURITY_MICRO_SERVICE, requestBody, httpHeaders);

        MicroserviceResponse response = (MicroserviceResponse) proceedingJoinPoint.proceed();

        String status = response.getStatus();

        logService.updateEndpointDetails(response, status, endpointDetails);

        String name = proceedingJoinPoint.getArgs().length > 2 ? proceedingJoinPoint.getSignature().getName() + ":" + proceedingJoinPoint.getArgs()[2] : proceedingJoinPoint.getSignature().getName();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
        {
            endpointDetails.setOtherInfo("Error in executing request for " + name + " in " + Constant.API_SECURITY_MICRO_SERVICE);

            logsData.getEndpointDetails().add(endpointDetails);

            throw new RouterException(response.getResponse());
        }

        endpointDetails.setOtherInfo("Successfully executed request for " + name +" in " + Constant.API_SECURITY_MICRO_SERVICE);

        logsData.getEndpointDetails().add(endpointDetails);

        return response;
    }
}
