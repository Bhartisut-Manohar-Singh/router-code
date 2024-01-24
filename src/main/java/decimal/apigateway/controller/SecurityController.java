package decimal.apigateway.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.Constants;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.security.SecurityServiceEnc;
import decimal.apigateway.service.validator.ValidatorFactory;
import decimal.logs.filters.AuditTraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(value = "security/" )
public class SecurityController {

    private SecurityServiceEnc securityServiceEnc;

    private final ValidatorFactory validatorFactory;

    @Autowired
    public SecurityController(SecurityServiceEnc securityServiceEnc, ValidatorFactory validatorFactory) {
        this.securityServiceEnc = securityServiceEnc;
        this.validatorFactory = validatorFactory;
    }

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    ObjectMapper objectMapper;


    @PostMapping(value = "validate/{validationType}")
    ResponseEntity<Object> validate(@RequestBody String request, @RequestHeader Map<String, String> httpHeaders, @PathVariable String validationType) throws RouterException, IOException {

        auditTraceFilter.setLogRequestAndResponse(false);
        System.out.println(" ====== validation type ====== " + validationType);
        validatorFactory.getValidator(validationType).validate(request, httpHeaders);

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus("success");
        response.setMessage("Validated");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        System.out.println(" ====== validation response ====== " + objectMapper.writeValueAsString(response));
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PostMapping(value = "encryptResponse")
    MicroserviceResponse encryptResponse(@RequestBody Object finalResponse, @RequestHeader Map<String, String> httpHeaders) {

        return securityServiceEnc.encryptResponse(finalResponse.toString(), httpHeaders);

    }
}
