package decimal.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ResponseOperations;
import decimal.apigateway.model.LogsData;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.AuthenticationClient;
import decimal.apigateway.service.clients.SecurityClient;
import decimal.apigateway.service.validator.RequestValidator;
import exception.RouterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@CrossOrigin
public class RegistrationServiceImpl implements RegistrationService {

    private SecurityClient securityClient;

    private AuthenticationClient authenticationClient;

    private ObjectMapper objectMapper;

    @Autowired
    ResponseOperations responseOperations;

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    LogsData logsData;

    @Autowired
    public RegistrationServiceImpl(SecurityClient securityClient, AuthenticationClient authenticationClient, ObjectMapper objectMapper) {
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        ObjectNode jsonNodes = objectMapper.convertValue(requestValidator.validateRegistrationRequest(request, httpHeaders), ObjectNode.class);

        String userName = jsonNodes.get("username").asText();

        httpHeaders.put("username", userName);

        logsData.setLoginId(userName);

        MicroserviceResponse registerResponse = authenticationClient.register(request, httpHeaders);

        Map<String, Object> rsaKeysMap = objectMapper.convertValue(registerResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        String jwtToken = String.valueOf(rsaKeysMap.get("jwtToken"));

        ObjectNode node = objectMapper.createObjectNode();

        response.addHeader("Authorization", "Bearer " + jwtToken);

        node.put("Authorization", "Bearer " + jwtToken);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("rsa", objectMapper.writeValueAsString(rsaKeysMap));

        String finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(responseMap)).toString();

        MicroserviceResponse responseHash = securityClient.generateResponseHash(finalResponse, httpHeaders);

        response.addHeader("hash", responseHash.getMessage());
        node.put("hash", responseHash.getMessage());

        logsData.setResponseHeaders(node);

        return finalResponse;
    }

    @Override
    public Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException, RouterException {

        MicroserviceResponse microserviceResponse = requestValidator.validateAuthentication(request, httpHeaders);

        httpHeaders.put("username", microserviceResponse.getMessage());

        logsData.setLoginId(microserviceResponse.getMessage());

        Object plainRequest = microserviceResponse.getResponse();

        MicroserviceResponse authenticateResponse = authenticationClient.authenticate(plainRequest, httpHeaders);

        Map<String, Object> authResponse = objectMapper.convertValue(authenticateResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        Object finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(authResponse));

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(finalResponse, httpHeaders);

        ObjectNode node = objectMapper.createObjectNode();

        node.put("Authorization", authResponse.get("jwtToken").toString());
        response.addHeader("Authorization", authResponse.get("jwtToken").toString());

        if (!encryptedResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS_STATUS)) {
            return new ResponseEntity<>(authenticateResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        MicroserviceResponse authResponseHash = securityClient.generateAuthResponseHash(finalResponse.toString(), httpHeaders);

        response.addHeader("hash", authResponseHash.getMessage());

        node.put("hash", authResponseHash.getMessage());

        return finalResponseMap;
    }

    @Override
    public Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws RouterException {
        MicroserviceResponse microserviceResponse = requestValidator.validateLogout(request, httpHeaders);

        httpHeaders.put("username", microserviceResponse.getResponse().toString());

        return authenticationClient.logout(httpHeaders).getResponse();
    }

    @Override
    public Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
        return authenticationClient.forceLogout(httpHeaders).getResponse();
    }
}
