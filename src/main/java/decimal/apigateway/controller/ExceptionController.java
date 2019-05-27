package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler(value = RouterException.class)

    public ResponseEntity<Object> handleRouterException(RouterException ex) {

       return new ResponseEntity<>(ex.getResponse(), HttpStatus.BAD_REQUEST);
    }
}
