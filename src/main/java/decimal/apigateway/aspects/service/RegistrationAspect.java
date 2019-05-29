package decimal.apigateway.aspects.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
@Aspect
public class RegistrationAspect
{
    private  final LogService logService;

    public RegistrationAspect(LogService logService) {
        this.logService = logService;
    }

    @Pointcut(value = "execution(* decimal.apigateway.service.RegistrationServiceImpl.*(..)) && args(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void beforeMethod(String request, Map<String, String> httpHeaders, HttpServletResponse response){}

    @Before(value = "beforeMethod(request, httpHeaders, response)", argNames = "request, httpHeaders, response")
    public void initializeLogs(String request, Map<String, String> httpHeaders, HttpServletResponse response)
    {
        logService.initiateLogsData(request, httpHeaders);
    }

    @AfterReturning(value = "execution(* decimal.apigateway.service.RegistrationServiceImpl.*(..))", returning = "response")
    public void updateLogs(Object response)
    {
        logService.updateLogsData(response, HttpStatus.OK.toString(), Constant.SUCCESS_STATUS);
    }
}
