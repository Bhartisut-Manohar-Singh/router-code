package decimal.apigateway.aspects.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class ExecutionAspect
{
    private final LogService logService;

    public ExecutionAspect(LogService logService) {
        this.logService = logService;
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..)) && args(request, httpHeaders)", argNames = "request, httpHeaders")
    public void beforeMethod(String request, Map<String, String> httpHeaders){}

    @Before(value = "beforeMethod(request, httpHeaders)", argNames = "request, httpHeaders")
    public void initializeLogs(String request, Map<String, String> httpHeaders)
    {
        logService.initiateLogsData(request, httpHeaders);
    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.ExecutionServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response)
    {
        logService.updateLogsData(response, HttpStatus.OK.toString(), Constant.SUCCESS_STATUS);
    }
}
