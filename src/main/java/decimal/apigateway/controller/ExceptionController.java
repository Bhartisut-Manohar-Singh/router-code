package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.VahanaKPIMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;

@RestControllerAdvice
public class ExceptionController {

    @Autowired
    ObjectMapper mapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private VahanaKPIMetrics vahanaKpiMetrics;

    @ExceptionHandler(value = RouterException.class)
    public ResponseEntity<Object> handleRouterException(RouterException ex) {

        ERROR_LOGGER.error("Some error occurred in api-gateway", ex);

        try {
            String errorMsg = ex.getErrorMessage() != null && !ex.getErrorMessage().equals("") ? ex.getErrorMessage() : "Generic Error Msg";
            String errorCode = ex.getErrorCode() != null && !ex.getErrorCode().equals("") ? ex.getErrorCode() : "Generic ErrorCode";
            this.vahanaKpiMetrics.persistMetrics(ConstantUtil.FAILURE_STATUS, errorCode ,errorMsg ,  System.currentTimeMillis(), new Long(mapper.writeValueAsString(ex).getBytes().length));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

       return new ResponseEntity<>(ex.getResponse(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest req)
    {
        ERROR_LOGGER.error("Some error occurred in api-gateway", ex);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", Constant.FAILURE_STATUS);
        errorResponse.put("message", "Some error occurred on router. Error is: " + ex.getMessage());
        errorResponse.put("errorType", "SYSTEM");
        errorResponse.put("errorHint", "See system logs for more detail");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
