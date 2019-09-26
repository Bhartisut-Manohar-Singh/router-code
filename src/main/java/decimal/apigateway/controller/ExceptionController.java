package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.exception.RouterException;
import decimal.common.micrometer.ConstantUtil;
import decimal.common.micrometer.VahanaKPIMetrics;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.SystemError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

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

        createErrorPayload(ex);

       return new ResponseEntity<>(ex.getResponse(), HttpStatus.BAD_REQUEST);
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;

    private void createErrorPayload(Exception ex) {

        SystemError systemError = new SystemError();

        if(ex instanceof RouterException)
        {
            RouterException exception = (RouterException) ex;

            Object response = exception.getResponse();

            if(response != null){
                ObjectNode jsonNodes = mapper.convertValue(response, ObjectNode.class);

                String statusCode = jsonNodes.get("status") != null ? jsonNodes.get("status").asText() : HttpStatus.BAD_REQUEST.toString();

                String message = jsonNodes.get("message") !=null ? jsonNodes.get("message").asText() : "Some error occurred when executing request";
                systemError.setErrorCode(statusCode);
                systemError.setMessage(message);
                systemError.setDetailedError(jsonNodes.toString());

            }else {

                systemError.setErrorCode(exception.getErrorCode() == null || exception.getErrorCode().isEmpty() ? HttpStatus.BAD_REQUEST.toString() : exception.getErrorCode());
                systemError.setMessage(exception.getMessage());
                systemError.setDetailedError(exception.getErrorHint());
            }
        }
        else {
            systemError.setErrorCode(HttpStatus.BAD_REQUEST.toString());
            systemError.setMessage("Some error occurred when executing request " + ex.getMessage());
        }

        ErrorPayload errorPayload = new ErrorPayload();
        errorPayload.setSystemError(systemError);
        errorPayload.setTimestamp(Instant.now());

        errorPayload.setRequestIdentifier(auditTraceFilter.requestIdentifier);

        LogsConnector.newInstance().error(errorPayload, ex);
    }

  /*  @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest req)
    {
        ERROR_LOGGER.error("Some error occurred in api-gateway", ex);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", Constant.FAILURE_STATUS);
        errorResponse.put("message", "Some error occurred on router. Error is: " + ex.getMessage());
        errorResponse.put("errorType", "SYSTEM");
        errorResponse.put("errorHint", "See system logs for more detail");

        createErrorPayload(ex);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }*/
}
