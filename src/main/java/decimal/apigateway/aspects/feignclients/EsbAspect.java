package decimal.apigateway.aspects.feignclients;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.EndpointDetails;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.service.LogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class EsbAspect
{
    @Autowired
    LogsData logsData;

    @Autowired
    LogService logService;

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.EsbClient.*(..)) && args(requestBody, httpHeaders)")
    public void feignClients(String requestBody, Map<String, String> httpHeaders){}

    @Around("feignClients(requestBody, httpHeaders)")
    public Object initiateRequestForEsb(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable
    {
        EndpointDetails endpointDetails = logService.initiateEndpoint(Constant.ESB, requestBody, httpHeaders);

        Object response =  proceedingJoinPoint.proceed();

        logService.updateEndpointDetails(response, Constant.SUCCESS_STATUS, endpointDetails);

        String name = proceedingJoinPoint.getArgs().length > 2 ? proceedingJoinPoint.getSignature().getName() + ":" + proceedingJoinPoint.getArgs()[2] : proceedingJoinPoint.getSignature().getName();

        endpointDetails.setOtherInfo("Successfully executed request for " + name +" in " + Constant.API_SECURITY_MICRO_SERVICE);

        logsData.getEndpointDetails().add(endpointDetails);

        return response;
    }
}
