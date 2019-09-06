package decimal.apigateway.aspects.feignclients;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.service.LogService;
import decimal.logs.model.Payload;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Aspect
public class EsbAspect {
    @Autowired
    LogsData logsData;

    @Autowired
    LogService logService;

    @Pointcut(value = "execution(* decimal.apigateway.service.clients.EsbClient.*(..)) && args(requestBody, httpHeaders)")
    public void feignClients(String requestBody, Map<String, String> httpHeaders) {
    }

    @Around("feignClients(requestBody, httpHeaders)")
    public Object initiateRequestForEsb(ProceedingJoinPoint proceedingJoinPoint, String requestBody, Map<String, String> httpHeaders) throws Throwable {
//        EndpointDetails endpointDetails = logService.initiateEndpoint(Constant.ESB, requestBody, httpHeaders);
        Payload payload = logService.initEndpoint(Constant.ESB, requestBody, httpHeaders);

        Object response = proceedingJoinPoint.proceed();

//        logService.updateEndpointDetails(response, Constant.SUCCESS_STATUS, endpointDetails);
        logService.updateEndpoint(response, Constant.SUCCESS_STATUS, payload);

        String name = proceedingJoinPoint.getArgs().length > 2 ? proceedingJoinPoint.getSignature().getName() + ":" + proceedingJoinPoint.getArgs()[2] : proceedingJoinPoint.getSignature().getName();

//        endpointDetails.setOtherInfo("Successfully executed request for " + name + " in " + Constant.API_SECURITY_MICRO_SERVICE);
//        logsData.getEndpointDetails().add(endpointDetails);
        payload.getResponse().setMessage("Successfully executed request for " + name + " in " + Constant.ESB);

        return response;
    }
}
