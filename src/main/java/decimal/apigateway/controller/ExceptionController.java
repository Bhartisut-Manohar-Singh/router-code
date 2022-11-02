package decimal.apigateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.ServiceMonitoringAudit;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.masking.JsonMasker;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.SystemError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static decimal.apigateway.commons.Constant.FAILURE_STATUS;
import static decimal.apigateway.commons.Constant.SUCCESS_STATUS;
import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;

@RestControllerAdvice
public class ExceptionController {

    @Autowired
    ObjectMapper mapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LogsConnector logsConnector;

    @Autowired
    ServiceMonitoringAudit serviceMonitoringAudit;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;

    @ExceptionHandler(value = RouterException.class)
    public ResponseEntity<Object> handleRouterException(RouterException ex) throws JsonProcessingException {

        System.out.println("================================In Router Exception==============================");

        boolean isLogoutSuccess = false;
        ERROR_LOGGER.error("Some error occurred in api-gateway", ex);

        ERROR_LOGGER.error("Error response: " + ex.getResponse());
        ex.printStackTrace();

        try {
            String errorMsg = ex.getErrorMessage() != null && !ex.getErrorMessage().equals("") ? ex.getErrorMessage() : "Generic Error Msg";
            String errorCode = ex.getErrorCode() != null && !ex.getErrorCode().equals("") ? ex.getErrorCode() : "Generic ErrorCode";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }

        try {
            createErrorPayload(ex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if(ex.getResponse() != null)
        {
            JsonNode jsonNode = mapper.convertValue(ex.getResponse(),JsonNode.class);
            isLogoutSuccess =  jsonNode.hasNonNull("status") ? jsonNode.get("status").asText().equalsIgnoreCase("625") : false;
        }

      /* if(auditPayload != null && auditPayload.getResponse()!=null) {
            auditPayload.getResponse().setResponse(ex.getResponse() != null ? mapper.writeValueAsString(ex.getResponse()) : "");
            auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            auditPayload.getResponse().setTimestamp(Instant.now());
            auditPayload.setStatus(isLogoutSuccess ? SUCCESS_STATUS : FAILURE_STATUS);
            logsWriter.updateLog(auditPayload);
        }*/
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status",isLogoutSuccess ? SUCCESS_STATUS : FAILURE_STATUS);
       return new ResponseEntity<>(ex.getResponse(), responseHeaders,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value =  HttpServerErrorException.class)
    public ResponseEntity<Object> handleHttpServerErrorException(HttpServerErrorException exception) throws JsonProcessingException {
        System.out.println("================================In Exception Controller==============================");
        exception.printStackTrace();

        String errorResponse = exception.getResponseBodyAsString();

        String message = "Some error occurred when executing request";
        String status = FAILURE_STATUS;

        MicroserviceResponse microserviceResponse = new MicroserviceResponse(status, message, errorResponse);
          try {
              createErrorPayload(exception);
          }
          catch (Exception e)
          {
              e.printStackTrace();
          }

        if(auditPayload != null && auditPayload.getResponse()!=null) {
            auditPayload.getResponse().setResponse(microserviceResponse.getResponse() != null ? mapper.writeValueAsString(microserviceResponse) : "");
            auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            auditPayload.getResponse().setTimestamp(Instant.now());
            auditPayload.setStatus(FAILURE_STATUS);
            logsWriter.updateLog(auditPayload);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status",FAILURE_STATUS);
        return new ResponseEntity<>(microserviceResponse, responseHeaders,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;
    private void createErrorPayload(Exception ex) {
        ex.printStackTrace();
        SystemError systemError = new SystemError();

        if(ex instanceof RouterException)
        {
            RouterException exception = (RouterException) ex;

            Object response = exception.getResponse();

            if(response != null){
                ObjectNode jsonNodes = mapper.convertValue(response, ObjectNode.class);

                String statusCode = jsonNodes.get("status") != null ? jsonNodes.get("status").asText() : HttpStatus.BAD_REQUEST.toString();

                String message = jsonNodes.get("message") !=null ? jsonNodes.get("message").toString() : "Some error occurred when executing request";
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


        logsConnector.error(errorPayload, ex);

        serviceMonitoringAudit.performAudit(errorPayload);
    }

/*    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest req) throws JsonProcessingException {
        ERROR_LOGGER.error("Some error occurred in api-gateway", ex);

        System.out.println("Is instance of RouterException " + (ex instanceof RouterException));

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", Constant.FAILURE_STATUS);
        errorResponse.put("message", "Some error occurred on router. Error is: " + ex.getMessage());
        errorResponse.put("errorType", "SYSTEM");
        errorResponse.put("errorHint", "See system logs for more detail");

        createErrorPayload(ex);

        auditPayload.getResponse().setResponse(mapper.writeValueAsString(errorResponse));
        auditPayload.getResponse().setStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()));
        auditPayload.getResponse().setTimestamp(Instant.now());
        auditPayload.setStatus(FAILURE_STATUS);

        logsWriter.updateLog(auditPayload);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status",FAILURE_STATUS);

        return new ResponseEntity<>(errorResponse, responseHeaders,HttpStatus.BAD_REQUEST);
    }*/
}
