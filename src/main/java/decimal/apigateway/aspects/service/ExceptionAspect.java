package decimal.apigateway.aspects.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.service.LogService;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.CustomEndpointMetrics;
import decimal.common.utils.CommonUtils;
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
    private CustomEndpointMetrics customEndpointMetrics;

    private final static ObjectMapper mapper = new ObjectMapper();

    public ExceptionAspect(LogService logService) {
        this.logService = logService;
    }

    @AfterReturning(value = "execution(* decimal.apigateway.controller.ExceptionController.*(..))", returning = "response")
    public void exceptionHandler(ResponseEntity<Object> response)
    {
        logService.updateLogsData(response.getBody(), response.getStatusCode().toString(), Constant.FAILURE_STATUS);

        try {
            this.customEndpointMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS,response.getStatusCode().toString() ,  CommonUtils.getCurrentUTC(), new Long(mapper.writeValueAsString(response.getBody()).getBytes().length));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
