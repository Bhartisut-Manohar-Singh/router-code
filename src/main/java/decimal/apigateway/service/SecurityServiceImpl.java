package decimal.apigateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.logs.model.AuditPayload;
import decimal.sessionmanagement.exception.RouterException;
import decimal.sessionmanagement.service.EncryptionDecryptionService;
import decimal.sessionmanagement.service.SecurityValidator;
import decimal.sessionmanagement.service.ValidationServiceV2;
import decimal.sessionmanagement.service.validator.ValidatorFactory;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
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
        try {
            return securityValidator.validateRegistration(request, httpHeaders).getResponse();
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
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.decryptRequestWithoutSession(request, httpHeaders);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse encryptResponseWithoutSession(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.encryptResponseWithoutSession(responseEntity.getBody().toString(), httpHeaders);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse generateResponseHash(String finalResponse, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateResponseHash(finalResponse, httpHeaders);
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
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = validatorFactory.getValidator(name).validate(request, httpHeaders);
            log.info("response from session Management "+microserviceResponse);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = securityValidator.validateAuthenticationRequest(request, httpHeaders);
            return new MicroserviceResponse(microserviceResponse);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse generateAuthResponseHash(String finalResponse, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateAuthResponseHash(finalResponse, httpHeaders);
            return new MicroserviceResponse(microserviceResponse);
        } catch (decimal.sessionmanagement.exception.RouterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) {
        try {
            auditPayload = logsWriter.initializeLog(request, JSON,httpHeaders, "api-security", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            logsWriter.updateLog(auditPayload);
            ResponseEntity response = securityValidator.validatePlainRequest(request, httpHeaders, serviceName);
            return new MicroserviceResponse(response.getStatusCode().toString(), "", response.getBody());
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse response = securityValidator.validateExecutionRequest(request, httpHeaders);
            return new MicroserviceResponse(response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object validatePublicRegistration(String request, Map<String, String> httpHeaders) {

        try {
         return securityValidator.validatePublicRegistration(request, httpHeaders);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }



    public MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders) {

        try {
            decimal.sessionmanagement.model.MicroserviceResponse response = securityValidator.validateAuthenticationRequestV2(request, httpHeaders);
            return new MicroserviceResponse(response);
        } catch (RouterException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
