package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.VahanaKPIMetrics;
import decimal.logs.model.ErrorPayload;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ExceptionAspect {

    private final LogService logService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VahanaKPIMetrics vahanaKpiMetrics;

    @Autowired
    ErrorPayload errorPayload;

    private final static ObjectMapper mapper = new ObjectMapper();

    public ExceptionAspect(LogService logService) {
        this.logService = logService;
    }

    @AfterReturning(value = "execution(* decimal.apigateway.controller.ExceptionController.*(..))", returning = "response")
    public void exceptionHandler(ResponseEntity<Object> response) {
        logService.createErrorPayload(response.getBody(), response.getStatusCode().toString(), Constant.FAILURE_STATUS);
        try {
            String errorMsg = response.getStatusCode().toString()!= null && !response.getStatusCode().toString().equals("") ? response.getStatusCode().toString() : "Generic Error Msg";
            String errorCode = response.getStatusCodeValue() != 0 ? Integer.toString(response.getStatusCodeValue()) : "Generic ErrorCode";
//            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS, errorCode ,errorMsg ,  CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response.getBody()).getBytes().length));
            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS, errorCode ,errorMsg ,  System.currentTimeMillis(), new Long(mapper.writeValueAsString(response.getBody()).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterReturning(value = "execution(* decimal.apigateway.controller.RegistrationController.*(..))", returning = "response")
    public void registerExceptionHandler(ResponseEntity<Object> response) {
        try {
            String errorMsg = response.getStatusCode().toString()!= null && !response.getStatusCode().toString().equals("") ? response.getStatusCode().toString() : "Generic Error Msg";
            String errorCode = response.getStatusCodeValue() != 0 ? Integer.toString(response.getStatusCodeValue()) : "Generic ErrorCode";
            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS, errorCode , errorMsg ,  System.currentTimeMillis(), new Long(mapper.writeValueAsString(response.getBody()).getBytes().length));
//            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS, errorCode , errorMsg ,  CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response.getBody()).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
