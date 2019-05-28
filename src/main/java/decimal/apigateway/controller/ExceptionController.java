package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constant;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionController {

    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler(value = RouterException.class)
    public ResponseEntity<Object> handleRouterException(RouterException ex) {

       return new ResponseEntity<>(ex.getResponse(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest req) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", Constant.FAILURE_STATUS);
        errorResponse.put("message", "Some error occurred on router. Error is: " + ex.getMessage());
        errorResponse.put("errorType", "SYSTEM");
        errorResponse.put("errorHint", "See system logs for more detail");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
