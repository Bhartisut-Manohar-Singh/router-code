package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.authenticationservice.exception.RouterExceptionAuth;
import decimal.authenticationservice.model.MicroserviceResponseAuth;
import decimal.authenticationservice.service.AuthenticationProcessor;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.commons.Constant.JSON;


@Service
@Log
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    AuthenticationProcessor authenticationProcessor;
    @Autowired
    ObjectMapper objectMapper;

    AuditPayload auditPayload;
    @Autowired
    LogsWriter logsWriter;




    @Override
    public ResponseEntity<Object> register(String request, Map<String, String> httpHeaders) {

        try {

            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders, "authentication-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            log.info(" ========== in register auth service =============" + auditPayload.hashCode());
            MicroserviceResponseAuth microserviceResponse = authenticationProcessor.register(request, httpHeaders);
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
        } catch (IOException | RouterExceptionAuth e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Object> authenticateV2(Object plainRequest, Map<String, String> httpHeaders) {

        try {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(null, JSON,httpHeaders, "authentication-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(plainRequest.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            log.info(" ============= inside authenticateV2 =================" + auditPayload.hashCode());
            MicroserviceResponseAuth microserviceResponse = authenticationProcessor.authenticateV2(plainRequest, httpHeaders);
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
        } catch (IOException | RouterExceptionAuth e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Object> forceLogout(Map<String, String> httpHeaders) {

        MicroserviceResponseAuth microserviceResponse = authenticationProcessor.forceLogout(httpHeaders);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", microserviceResponse.getStatus());
        return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> logout(Map<String, String> httpHeaders) {

        MicroserviceResponseAuth response = new MicroserviceResponseAuth();

        try {
            log.info("logout------httpHeaders" + httpHeaders);
            authenticationProcessor.logout(httpHeaders);
            log.info("logout microserviceResponse------------------------ ");

        } catch (RouterExceptionAuth e) {
            log.info("exception printing--------" + e.getMessage());
            String errorCode = "625";
            if (errorCode.equals(e.getErrorCode())) {
                response.setStatus("SUCCESS");
                response.setMessage("User has been logout successfully");
                response.setResponse(errorCode);
            } else {
                response.setStatus("FAILURE");
                response.setMessage("User has not been logout successfully");
                response.setResponse(errorCode);
            }
        } catch (JsonProcessingException je) {
            throw new RuntimeException("Something went wrong");
        }
        log.info("status---------" + response.getStatus());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> authenticate(Object plainRequest, Map<String, String> httpHeaders) {

        try {
            MicroserviceResponseAuth microserviceResponse = authenticationProcessor.authenticate(String.valueOf(plainRequest), httpHeaders);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
        } catch (IOException | RouterExceptionAuth e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Object> publicRegister(String request, Map<String, String> httpHeaders) {

        try {
            MicroserviceResponseAuth response = authenticationProcessor.publicRegister(request, httpHeaders);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", response.getStatus());
            return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);
        } catch (IOException | RouterExceptionAuth e) {
            throw new RuntimeException(e);
        }
    }

    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

    }
}
