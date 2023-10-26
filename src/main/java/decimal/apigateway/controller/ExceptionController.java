package decimal.apigateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.exception.PublicTokenCreationException;
import decimal.apigateway.exception.RouterExceptionV1;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.model.ResponseOutput;
import decimal.apigateway.service.LogsWriter;
import decimal.apigateway.service.ServiceMonitoringAudit;
import decimal.authenticationservice.exception.RouterExceptionAuth;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.SystemError;
import decimal.ratelimiter.exception.RequestNotPermitted;
import decimal.sessionmanagement.exception.RouterException;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.time.Instant;

import static decimal.apigateway.commons.Constant.*;
import static decimal.apigateway.commons.Loggers.ERROR_LOGGER;
import static decimal.authenticationservice.common.ConstantsAuth.STATUS;
import static decimal.sessionmanagement.common.RouterResponseCode.ROUTER_MULTIPLE_SESSION;

@RestControllerAdvice
@Log
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

    @ExceptionHandler({RouterExceptionV1.class, RouterException.class})
    public ResponseEntity<Object> handleRouterException(RouterExceptionV1 ex) throws JsonProcessingException {

        log.info("================================In Router Exception==============================");

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
        log.info("================================In Exception Controller==============================");
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

        if(ex instanceof RouterExceptionV1)
        {
            RouterExceptionV1 exception = (RouterExceptionV1) ex;

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

        log.info("Is instance of RouterException " + (ex instanceof RouterException));

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

    @ExceptionHandler(value = RequestNotPermitted.class)
    public ResponseEntity<Object> handleRouterException(RequestNotPermitted ex) throws JsonProcessingException {
        log.info("Inside request not permission exception handler - " + ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), null, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(value = PublicTokenCreationException.class)
    public ResponseEntity<Object> handlePublicJwtCreationException(PublicTokenCreationException ex)  {
        log.info("Inside handlePublicJwtCreationException - " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(new ResponseOutput(ex.getErrorCode(), ex.getErrorMessage()), null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = RouterException.class)
    public ResponseEntity<Object> handleIOException(RouterException ex)  {
        log.info("Inside IOException - " + ex.getCause().getMessage());
        ex.printStackTrace();
        RouterException response=new RouterException();
        //MicroserviceResponse response = new MicroserviceResponse();
        response.setErrorCode(ex.getErrorCode());
        response.setErrorMessage(ex.getErrorMessage());
        response.setErrorHint(ex.getErrorHint());
        response.setErrorType(ex.getErrorType());
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<>(response,responseHeaders, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = RouterExceptionAuth.class)
    public ResponseEntity<Object> handleRouterExceptionAUTH(RouterExceptionAuth ex) throws JsonProcessingException {

        log.info("Inside handleRouterExceptionAUTH exception handler - " + ex.getMessage());

        RouterExceptionAuth  exception = new RouterExceptionAuth();
        exception.setErrorCode(ex.getErrorCode());
        exception.setErrorHint(ex.getErrorHint());
        exception.setErrorMessage(exception.getErrorMessage());

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(ex.getErrorCode());
        response.setMessage(ex.getErrorMessage());
        response.setResponse(String.valueOf(ex.getErrorHint()));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(STATUS,ROUTER_MULTIPLE_SESSION);
        return new ResponseEntity<>(response,responseHeaders, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Object> handleRouterExceptionAUTH(RuntimeException ex) throws JsonProcessingException {

        log.info(" Inside handleRuntimeException - " + ex.getMessage());
        ex.printStackTrace();

        MicroserviceResponse response = new MicroserviceResponse();
        response.setMessage(ex.getMessage());
        response.setResponse("RunTimeException");
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<>(response,responseHeaders, HttpStatus.BAD_REQUEST);
    }


}
