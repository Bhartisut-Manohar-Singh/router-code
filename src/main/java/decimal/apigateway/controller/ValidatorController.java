package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constants;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.validator.Validator;
import decimal.apigateway.service.validator.ValidatorFactory;
import decimal.logs.filters.AuditTraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("engine/v1/")
public class ValidatorController {



    private final ValidatorFactory validatorFactory;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    public ValidatorController(ValidatorFactory validatorFactory){
        this.validatorFactory=validatorFactory;
    }


    @PostMapping("validate/{validationType}")
    MicroserviceResponse validate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String validationType) throws RouterException, IOException {

        auditTraceFilter.setLogRequestAndResponse(false);
        System.out.println(" ====== validation type ====== " + validationType);
        MicroserviceResponse response = validatorFactory.getValidator(validationType).validate(request, httpHeaders);
        System.out.println(" ====== validation response ====== " + objectMapper.writeValueAsString(response));

        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage("Validation has been done of type: " + validationType);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        return response;

    }
}
