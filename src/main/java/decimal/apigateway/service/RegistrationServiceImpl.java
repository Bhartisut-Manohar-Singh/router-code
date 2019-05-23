package decimal.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.commons.ResponseOperations;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.service.clients.AuthenticationClient;
import decimal.apigateway.service.clients.SecurityClient;
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
    public RegistrationServiceImpl(SecurityClient securityClient, AuthenticationClient authenticationClient, ObjectMapper objectMapper) {
        this.securityClient = securityClient;
        this.authenticationClient = authenticationClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object register(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {
        MicroserviceResponse microserviceResponse = securityClient.validateRegistration(request, httpHeaders);

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(microserviceResponse.getStatus())) {
           return new ResponseEntity<>(microserviceResponse.getResponse(), HttpStatus.BAD_REQUEST);

        }

        ObjectNode jsonNodes = objectMapper.convertValue(microserviceResponse.getResponse(), ObjectNode.class);

        String userName = jsonNodes.get("username").asText();

        httpHeaders.put("username", userName);

        MicroserviceResponse registerResponse = authenticationClient.register(request, httpHeaders);

        String status = registerResponse.getStatus();

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
        {
            return new ResponseEntity<>(registerResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> rsaKeysMap = objectMapper.convertValue(registerResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        String jwtToken = String.valueOf(rsaKeysMap.get("jwtToken"));

        response.addHeader("Authorization", "Bearer " + jwtToken);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("rsa", objectMapper.writeValueAsString(rsaKeysMap));

        String finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(responseMap)).toString();

        MicroserviceResponse responseHash = securityClient.generateResponseHash(finalResponse, httpHeaders);

        status = responseHash.getStatus();

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
        {
            return new ResponseEntity<>(responseHash.getResponse(), HttpStatus.BAD_REQUEST);
        }

        response.addHeader("hash", responseHash.getMessage());

        return finalResponse;
    }

    @Autowired
    LoginDetailsStorage loginDetailsStorage;

    @Override
    public Object authenticate(String request, Map<String, String> httpHeaders, HttpServletResponse response) throws IOException {
        MicroserviceResponse microserviceResponse = securityClient.validateAuthentication(request, httpHeaders);

        String status = microserviceResponse.getStatus();

        if (!Constant.SUCCESS_STATUS.equalsIgnoreCase(status)) {
            return new ResponseEntity<>(microserviceResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        Object plainRequest = microserviceResponse.getResponse();

        httpHeaders.put("username", microserviceResponse.getMessage());

        MicroserviceResponse authenticateResponse = authenticationClient.authenticate(plainRequest, httpHeaders);

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(authenticateResponse.getStatus()))
        {
            return new ResponseEntity<>(authenticateResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> authResponse = objectMapper.convertValue(authenticateResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        System.out.println("Auth response  " + authResponse);

        Object finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(authResponse));

        MicroserviceResponse encryptedResponse = securityClient.encryptResponse(finalResponse, httpHeaders);

        response.addHeader("Authorization", authResponse.get("jwtToken").toString());

        if(!encryptedResponse.getStatus().equalsIgnoreCase(Constant.SUCCESS_STATUS))
        {
            return new ResponseEntity<>(authenticateResponse.getResponse(), HttpStatus.BAD_REQUEST);
        }

        Map<String, String> finalResponseMap = new HashMap<>();
        finalResponseMap.put("response", encryptedResponse.getMessage());

        MicroserviceResponse authResponseHash = securityClient.generateAuthResponseHash(finalResponse.toString(), httpHeaders);

        System.out.println(authenticateResponse);

        status = authResponseHash.getStatus();

        if(!Constant.SUCCESS_STATUS.equalsIgnoreCase(status))
        {
            return new ResponseEntity<>(authResponseHash.getResponse(), HttpStatus.BAD_REQUEST);
        }

        response.addHeader("hash", authResponseHash.getMessage());

        loginDetailsStorage.storeLoginDetails(plainRequest, httpHeaders);

        return finalResponseMap;
    }

    @Override
    public Object logout(String request, Map<String, String> httpHeaders, HttpServletResponse response)
    {
        return authenticationClient.logout(httpHeaders).getResponse();
    }

    @Override
    public Object forceLogout(String request, Map<String, String> httpHeaders, HttpServletResponse response) {
        return authenticationClient.forceLogout(httpHeaders).getResponse();
    }
}
