package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.authentication.AuthenticationProcessor;
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
    public ResponseEntity<Object> register(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders, "authentication-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            log.info(" ========== in register auth service =============" + auditPayload.hashCode());
            MicroserviceResponse microserviceResponse = authenticationProcessor.register(request, httpHeaders);
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Object> authenticateV2(Object plainRequest, Map<String, String> httpHeaders) throws RouterException, IOException {

            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(null, JSON,httpHeaders, "authentication-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(plainRequest.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            log.info(" ============= inside authenticateV2 =================" + auditPayload.hashCode());
            MicroserviceResponse microserviceResponse = authenticationProcessor.authenticateV2(plainRequest, httpHeaders);
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> forceLogout(Map<String, String> httpHeaders) {

        MicroserviceResponse microserviceResponse = authenticationProcessor.forceLogout(httpHeaders);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", microserviceResponse.getStatus());
        return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> logout(Map<String, String> httpHeaders) {

        MicroserviceResponse response = new MicroserviceResponse();

        try {
            log.info("logout------httpHeaders" + httpHeaders);
            authenticationProcessor.logout(httpHeaders);
            log.info("logout microserviceResponse------------------------ ");

        } catch (RouterException e) {
            log.info("exception printing--------" + e.getMessage());
            String errorCode = "625";
            if (errorCode.equals(e.getErrorCode())) {
                response.setStatus(errorCode);
                response.setMessage("User has been logout successfully");
                response.setResponse("SUCCESS");
            } else {
                response.setStatus(errorCode);
                response.setMessage("User has not been logout successfully");
                response.setResponse("FAILURE");
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
    public ResponseEntity<Object> authenticate(Object plainRequest, Map<String, String> httpHeaders) throws RouterException, IOException {

            MicroserviceResponse microserviceResponse = authenticationProcessor.authenticate(String.valueOf(plainRequest), httpHeaders);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            return new ResponseEntity<>(microserviceResponse, responseHeaders, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Object> publicRegister(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

            MicroserviceResponse response = authenticationProcessor.publicRegister(request, httpHeaders);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", response.getStatus());
            return new ResponseEntity<>(response, responseHeaders, HttpStatus.OK);

    }

    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

    }
}
