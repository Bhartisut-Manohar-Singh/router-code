
package decimal.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.security.EncryptionDecryptionService;
import decimal.apigateway.service.security.SecurityValidator;
import decimal.apigateway.service.security.ValidationServiceV2;
import decimal.apigateway.service.validator.ValidatorFactory;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${isSecurityLogsEnabled:N}")
    String securityLogsEnabled;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Object validateRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

            return securityValidator.validateRegistration(request, httpHeaders).getResponse();

    }

    @Override
    public MicroserviceResponse validateExecutionRequestV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }

            MicroserviceResponse microserviceResponse = (MicroserviceResponse) validationServiceV2.validateExecutionRequest(request, httpHeaders).getBody();

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;


    }


    @Override
    public MicroserviceResponse decryptRequest(JsonNode node, Map<String, String> httpHeaders) throws RouterException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(node.toString(), JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(node.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }

            MicroserviceResponse microserviceResponse = encryptionDecryptionService.decryptRequest(node.asText(), httpHeaders);


        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }

        return microserviceResponse;

    }


    @Override
    public MicroserviceResponse decryptRequestWithoutSession(String request, Map<String, String> httpHeaders) throws RouterException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse microserviceResponse = encryptionDecryptionService.decryptRequestWithoutSession(request, httpHeaders);

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;

    }

    @Override
    public MicroserviceResponse encryptResponseWithoutSession(ResponseEntity<Object> responseEntity, Map<String, String> httpHeaders) throws RouterException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(responseEntity.toString(), JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(responseEntity.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
        MicroserviceResponse microserviceResponse = null;
        try {
            microserviceResponse = encryptionDecryptionService.encryptResponseWithoutSession(objectMapper.writeValueAsString(responseEntity.getBody()), httpHeaders);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;
    }

    @Override
    public MicroserviceResponse generateResponseHash(String finalResponse, Map<String, String> httpHeaders) throws RouterException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(finalResponse, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(finalResponse.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateResponseHash(finalResponse, httpHeaders);


        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;

    }

    @Override
    public MicroserviceResponse validate(String request, Map<String, String> httpHeaders, String name) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse microserviceResponse = validatorFactory.getValidator(name).validate(request, httpHeaders);


        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;

    }

    @Override
    public MicroserviceResponse validateAuthentication(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse microserviceResponse = securityValidator.validateAuthenticationRequest(request, httpHeaders);

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;

    }

    @Override
    public MicroserviceResponse generateAuthResponseHash(String finalResponse, Map<String, String> httpHeaders) throws RouterException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(finalResponse, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(finalResponse.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse microserviceResponse = encryptionDecryptionService.generateAuthResponseHash(finalResponse, httpHeaders);

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(microserviceResponse.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(microserviceResponse.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", microserviceResponse.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return microserviceResponse;

    }

    @Override
    public MicroserviceResponse validatePlainRequest(String request, Map<String, String> httpHeaders, String serviceName) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request);
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
            logsWriter.updateLog(auditPayload);
        }
            ResponseEntity response = securityValidator.validatePlainRequest(request, httpHeaders, serviceName);
            log.info("====== inside validatePlainRequest =======" + new Gson().toJson(response.getBody()));
            return new MicroserviceResponse(response.getStatusCode().toString(), "", response.getBody());

    }

    @Override
    public MicroserviceResponse validateExecutionRequest(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }
            MicroserviceResponse response = securityValidator.validateExecutionRequest(request, httpHeaders);

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(response.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(response.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", response.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return response;
    }

    @Override
    public Object validatePublicRegistration(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        return securityValidator.validatePublicRegistration(request, httpHeaders);
    }



    public MicroserviceResponse validateAuthenticationV2(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload = auditPayload();
            auditPayload = logsWriter.initializeLog(request, JSON, httpHeaders, "security-service", auditPayload);
            auditPayload.getRequest().setHeaders(httpHeaders);
            auditPayload.getRequest().setRequestBody(request.toString());
            auditPayload.getRequestIdentifier().setAppId(httpHeaders.get("appid"));
            auditPayload.getRequestIdentifier().setOrgId(httpHeaders.get("orgid"));
        }

            MicroserviceResponse response = securityValidator.validateAuthenticationRequestV2(request, httpHeaders);

        if (securityLogsEnabled.equalsIgnoreCase("Y")) {
            auditPayload.setStatus(response.getStatus());
            auditPayload.getResponse().setResponse(String.valueOf(response.getResponse()));
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("status", response.getStatus());
            logsWriter.updateLog(auditPayload);
        }
            return response;

    }

    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

    }

}
