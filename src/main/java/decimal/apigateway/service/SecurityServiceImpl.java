package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import decimal.sessionmanagement.exception.RouterException;
import decimal.sessionmanagement.service.EncryptionDecryptionService;
import decimal.sessionmanagement.service.SecurityValidator;
import decimal.sessionmanagement.service.ValidationServiceV2;
import decimal.sessionmanagement.service.validator.ValidatorFactory;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static decimal.apigateway.commons.Constant.JSON;

@Service
@Log
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    EncryptionDecryptionService encryptionDecryptionService;

    @Autowired
    SecurityValidator securityValidator;

    @Autowired
    ValidatorFactory validatorFactory;

    @Autowired
    ValidationServiceV2 validationServiceV2;

    @Autowired
    AuditPayload auditPayload;

    @Autowired
    LogsWriter logsWriter;


    @Override
    public Object validateRegistration(String request, Map<String, String> httpHeaders) {
        /*auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders);
        auditPayload.getRequest().setHeaders(httpHeaders);
        auditPayload.getRequest().setRequestBody(request);
        auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
        auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));*/
        //logsWriter.updateLog(auditPayload);

        try {
            auditPayload=auditPayload();
            auditPayload = logsWriter.initializeLog(request,request,httpHeaders,"api-security123",auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            logsWriter.updateLog(auditPayload);
            Object response= securityValidator.validateRegistration(request, httpHeaders).getResponse();
            responseFromSecurity((decimal.sessionmanagement.model.MicroserviceResponse) response);
            return new MicroserviceResponse((decimal.sessionmanagement.model.MicroserviceResponse) response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validateExecutionRequestV2(String request, Map<String, String> httpHeaders) {

        try {
            log.info(httpHeaders.get("sourceorgid"));
            log.info(httpHeaders.get("sourceorgid"));
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = (decimal.sessionmanagement.model.MicroserviceResponse) validationServiceV2.validateExecutionRequest(request, httpHeaders).getBody();
            return new MicroserviceResponse(microserviceResponse);

        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public MicroserviceResponse decryptRequest(JsonNode node, Map<String, String> httpHeaders) {
        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.decryptRequest(node.asText(), httpHeaders);
            log.info("MicroserviceResponse  " + microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse encryptResponse(String body, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.encryptResponse(body, httpHeaders);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders) {

        try {
            logsForSecurity(request, request, httpHeaders, "api-security123");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.decryptRequestWithoutSession(request, httpHeaders);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public MicroserviceResponse encryptResponseWithoutSession(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) {

        try {
            logsForSecurity(null,responseEntity.getBody().toString(),httpHeaders,"api-security123");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.encryptResponseWithoutSession(responseEntity.getBody().toString(), httpHeaders);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse generateResponseHash(String finalResponse, Map<String, String> httpHeaders) {

        try {
            logsForSecurity(finalResponse,finalResponse,httpHeaders,"api-security123");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateResponseHash(finalResponse, httpHeaders);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders, String name) {

        try {
            log.info("request----"+request);
            log.info("httpHeaders------"+httpHeaders+"    name-----------"+name);
            logsForSecurity(request, request, httpHeaders, "api-security123");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = validatorFactory.getValidator(name).validate(request, httpHeaders);
            log.info("response from session Management "+microserviceResponse);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) {

        try {
            logsForSecurity(request, request, httpHeaders, "api-security123");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = securityValidator.validateAuthenticationRequest(request, httpHeaders);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse generateAuthResponseHash(String finalResponse, Map<String, String> httpHeaders) {

        try {
            logsForSecurity(finalResponse, finalResponse, httpHeaders, "api-security");
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateAuthResponseHash(finalResponse, httpHeaders);
            responseFromSecurity(microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) {
        try {
            logsForSecurity(request, request, httpHeaders, "api-security");
            ResponseEntity response = securityValidator.validatePlainRequest(request, httpHeaders, serviceName);
            auditPayload.setStatus(response.getStatusCode().toString());

            auditPayload.getResponse().setResponse(response.getBody().toString());
            logsWriter.updateLog(auditPayload);
            return new MicroserviceResponse(response.getStatusCode().toString(), "", response.getBody());
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders) {

        try {
            auditPayload = auditPayload();
            logsForSecurity(null, request, httpHeaders, "security-service1234");
            log.info(" ============= inside securityValidateAuthenticationV2 =================" + auditPayload.hashCode());
            decimal.sessionmanagement.model.MicroserviceResponse response = securityValidator.validateExecutionRequest(request, httpHeaders);
            responseFromSecurity(response);
            return new MicroserviceResponse(response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object validatePublicRegistration(String request, Map<String, String> httpHeaders) {

        try {
            auditPayload = auditPayload();
            logsForSecurity(null, request, httpHeaders, "security-service1234");
            log.info(" ============= inside validatePublicRegistration =================" + auditPayload.hashCode());
            decimal.sessionmanagement.model.MicroserviceResponse response= securityValidator.validatePublicRegistration(request, httpHeaders);
            responseFromSecurity(response);
            return  new MicroserviceResponse(response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }



    public MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders) {

        try {
            auditPayload = auditPayload();
            logsForSecurity(null, request, httpHeaders, "security-service1234");
            log.info(" ============= inside securityValidateAuthenticationV2 =================" + auditPayload.hashCode());
            decimal.sessionmanagement.model.MicroserviceResponse response = securityValidator.validateAuthenticationRequestV2(request, httpHeaders);
            responseFromSecurity(response);
            return new MicroserviceResponse(response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logsForSecurity(String request, String request1, Map<String, String> httpHeaders, String servicaName) {
        auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, servicaName, auditPayload);
        auditPayload.getRequest().setHeaders(httpHeaders);
        auditPayload.getRequest().setRequestBody(request1);
        auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
        auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
    }
    private void responseFromSecurity(decimal.sessionmanagement.model.MicroserviceResponse response) {
        auditPayload.setStatus(response.getStatus());

        auditPayload.getResponse().setResponse(String.valueOf(response.getResponse()));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("status", response.getStatus());
        logsWriter.updateLog(auditPayload);
    }


    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

    }


}
