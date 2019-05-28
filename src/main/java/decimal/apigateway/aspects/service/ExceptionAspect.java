package decimal.apigateway.aspects.service;

import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ExceptionAspect {

    private final LogService logService;

    public ExceptionAspect(LogService logService) {
        this.logService = logService;
    }

    @AfterReturning(value = "execution(* decimal.apigateway.controller.ExceptionController.*(..))", returning = "response")
    public void exceptionHandler(ResponseEntity<Object> response)
    {
        logService.updateLogsData(response.getBody(), response.getStatusCode().toString(), Constant.FAILURE_STATUS);
    }
}
