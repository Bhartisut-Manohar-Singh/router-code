package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.ResponseOperationsAuth;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.model.MicroserviceResponse;
import decimal.apigateway.repository.SSOTokenRepo;
import decimal.apigateway.service.AuthApplicationDefConfig;
import decimal.apigateway.service.authentication.sessionmgmt.AuthenticationSessionService;
import decimal.apigateway.service.authentication.sessionmgmt.LogoutService;
import decimal.apigateway.service.authentication.sessionmgmt.MultipleSession;
import decimal.apigateway.service.util.EventRequestUtil;
import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.EventRequest;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.commons.AuthRouterOperations;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class AuthenticationProcessorImpl implements AuthenticationProcessor {

    @Autowired
    private KeysGenerator keysGenerator;

    @Autowired
    private AuthenticationSessionService authenticationSessionService;

    @Autowired
    private UserAuthentication userAuthentication;

    @Autowired
    AuthApplicationDefConfig applicationDefConfig;

    @Autowired
    MultipleSession multipleSession;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ResponseOperationsAuth responseOperations;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    LogsConnector logsConnector;

    @Autowired
    LogoutService logoutService;

    @Autowired
    private HttpServiceCall httpServiceCall;

    @Value("${isAnalyticsRequired}")
    private String isAnalyticsRequired;

    @Value("${raise.event.topic}")
    private String raiseEventTopic;

    @Autowired
    SSOTokenRepo ssoTokenRepo;

    @Autowired
    EventRequestUtil eventRequestUtil;

    @Override
    public MicroserviceResponse register(String request, Map<String, String> httpHeaders) throws RouterException, IOException {
        Map<String, Object> rsaKeys = keysGenerator.generateTokenAndRsaKeys(httpHeaders);

        authenticationSessionService.createAndSaveAppSession(httpHeaders, rsaKeys);

        MicroserviceResponse microserviceResponse = new MicroserviceResponse();
        microserviceResponse.setStatus(SUCCESS_STATUS);
        microserviceResponse.setMessage("Keys has been generated successfully");
        rsaKeys.remove("private-modules");
        rsaKeys.remove("private-exponent");

        microserviceResponse.setResponse(rsaKeys);

        /*
        if ("Y".equalsIgnoreCase(isAnalyticsRequired))
        {
            EventRequest eventRequest = eventRequestUtil.setRegisterDetails(objectMapper.readValue(request, ObjectNode.class), httpHeaders);

            log.info("-----------------------------recieved event request--------------------------");

            logsConnector.raiseEvent(eventRequest);

        }

         */

        return microserviceResponse;
    }

    @Override
    public MicroserviceResponse authenticate(String request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("Start executing request for authentication");

        System.out.println("------------------Start executing request for authentication--------------------- ");

        String requestId = httpHeaders.get(Headers.requestid.name());
        String loginType = httpHeaders.get(Headers.auth_scheme.name());

        log.info("requestId--------------"+requestId +"loginType---------------"+loginType);

        Object authenticationResponse = null;
        try {
            authenticationResponse = userAuthentication.authenticate(request, httpHeaders,loginType);
        }catch (Exception e){
            System.out.println("====Exception In Authenticate Call====" + e.getMessage());
            throw e;
        }

        //Source Set
        multipleSession.validateMultipleSession(httpHeaders);

        Map<String, Object> rsaKeys = keysGenerator.getTokenAndRsaKeys(httpHeaders);

        authenticationSessionService.createUserSession(httpHeaders, rsaKeys);

        List<String> clientId = AuthRouterOperations.getStringArray(httpHeaders.get(CLIENT_ID), "~");

        String orgId = clientId.get(0);
        String appId = clientId.get(1);

        ApplicationDef byOrgIdAndAppId = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);

        String token = "Bearer "  + rsaKeys.get("jwtToken");

        if (byOrgIdAndAppId.getIsUserInactiveSessionRequired() != null && byOrgIdAndAppId.getIsUserInactiveSessionRequired().equalsIgnoreCase("Y")) {
            authenticationSessionService.storeInactiveSessionDetails(httpHeaders.get("username"), requestId, byOrgIdAndAppId.getUserInactiveSessionExpiryTime());
        }

        log.info("Returning authentication response to client " );

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("auth", authenticationResponse);
        authResponse.put("jwtToken", token);

        rsaKeys.remove("private-modules");
        rsaKeys.remove("private-exponent");

        authResponse.put("rsa", new Gson().toJson(rsaKeys));

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(SUCCESS_STATUS);
        response.setMessage("Authentication has been done successfully");
        response.setResponse(authResponse);


        if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
            EventRequest eventRequest=eventRequestUtil.setLoginLogoutDetails(objectMapper.readValue(request, ObjectNode.class), httpHeaders, LOGIN_SUCCESS_EVENT_NAME);

            eventRequest.setEventRemarks(loginType);
            if(SSO.equalsIgnoreCase(loginType)){
                Map<String,Object> eventData=new HashMap<>();
                eventData.put(REQUEST,objectMapper.readValue(request, JsonNode.class));
                eventData.put(RESPONSE,response);
                eventRequest.setEventData(eventData);
            }

            log.info("-----------------------------recieved event request--------------------------");

            logsConnector.raiseEvent(eventRequest,raiseEventTopic);
        }

        return response;
    }


    @Override
    public MicroserviceResponse authenticateV2(Object request, Map<String, String> httpHeaders) throws RouterException, IOException {

        log.info("Start executing request for authentication");

        System.out.println("------------------Start executing request for authentication--------------------- ");

        String requestId = httpHeaders.get(Headers.requestid.name());
        String loginType = httpHeaders.get(Headers.auth_scheme.name());

        Object authenticationResponse = null;
        try {
            authenticationResponse = userAuthentication.authenticateV2(request, httpHeaders,loginType);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("====Exception In Authenticate Call====" + e.getMessage());
            throw e;
        }

        multipleSession.validateMultipleSession(httpHeaders);

        Map<String, Object> rsaKeys = keysGenerator.getTokenAndRsaKeys(httpHeaders);

        authenticationSessionService.createUserSession(httpHeaders, rsaKeys);

        List<String> clientId = AuthRouterOperations.getStringArray(httpHeaders.get(CLIENT_ID), "~");

        String orgId = clientId.get(0);
        String appId = clientId.get(1);

        ApplicationDef byOrgIdAndAppId = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);

        String token = "Bearer "  + rsaKeys.get("jwtToken");

        if (byOrgIdAndAppId.getIsUserInactiveSessionRequired() != null && byOrgIdAndAppId.getIsUserInactiveSessionRequired().equalsIgnoreCase("Y")) {
            authenticationSessionService.storeInactiveSessionDetails(httpHeaders.get("username"), requestId, byOrgIdAndAppId.getUserInactiveSessionExpiryTime());
        }

        log.info("Returning authentication response to client " );

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("auth", authenticationResponse);
        authResponse.put("jwtToken", token);

        rsaKeys.remove("private-modules");
        rsaKeys.remove("private-exponent");

        authResponse.put("rsa", new Gson().toJson(rsaKeys));

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(SUCCESS_STATUS);
        response.setMessage("Authentication has been done successfully");
        response.setResponse(authResponse);


        if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
            EventRequest eventRequest=eventRequestUtil.setLoginLogoutDetailsV2(objectMapper.readValue((String) request, ObjectNode.class), httpHeaders, LOGIN_SUCCESS_EVENT_NAME);

            eventRequest.setEventRemarks(loginType);
            if(SSO.equalsIgnoreCase(loginType)){
                Map<String,Object> eventData=new HashMap<>();
                eventData.put(REQUEST,objectMapper.readValue((String) request, JsonNode.class));
                eventData.put(RESPONSE,response);
                eventRequest.setEventData(eventData);
            }

            log.info("-----------------------------recieved event request--------------------------");

            logsConnector.raiseEvent(eventRequest,raiseEventTopic);
        }

        return response;
    }


    @Override
    public MicroserviceResponse logout(Map<String, String> httpHeaders) throws RouterException, JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("status", AuthRouterResponseCode.ROUTER_KILL_SESSION_SUCCESS);
        map.put("errorHint", "User has been logout successfully");
        map.put("message","Session Killed Successfully");
        map.put("errorType", SUCCESS_STATUS);
        RouterException routerException = new RouterException(map);
        logoutService.doLogout(httpHeaders);
        throw routerException;
    }


    @Override
    public MicroserviceResponse forceLogout(Map<String, String> httpHeaders)
    {
        String orgId = httpHeaders.get(Headers.orgid.name());
        String appId = httpHeaders.get(Headers.appid.name());
        String sessionType = httpHeaders.get(ROUTER_HEADER_SESSION_TYPE);

        boolean isAllSessionsKilled;
        if(ROUTER_HEADER_USER_SESSION.equalsIgnoreCase(sessionType))
        {
            String loginId = httpHeaders.get(Headers.loginid.name());
            isAllSessionsKilled = multipleSession.killAllUserSessions(orgId, appId, loginId);
        }
        else {
            String deviceId = httpHeaders.get(Headers.deviceid.name());
            isAllSessionsKilled = multipleSession.killAllAppSessions(orgId, appId, deviceId);
        }

        String status = isAllSessionsKilled ? SUCCESS_STATUS : FAILURE_STATUS;
        String message = isAllSessionsKilled ? "All sessions has been killed associated with " : "Unable to kill all the sessions";

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(status);
        response.setMessage(message);

        return response;
    }

    @Override
    public MicroserviceResponse registerDevice(String request, Map<String, String> httpHeaders) throws IOException, RouterException {
        MicroserviceResponse response = (MicroserviceResponse) register(request, httpHeaders);

        Map<String, Object> rsaKeysMap = objectMapper.convertValue(response.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        String jwtToken = String.valueOf(rsaKeysMap.get("jwtToken"));

        jwtToken =  "Bearer " + jwtToken;

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("rsa", objectMapper.writeValueAsString(rsaKeysMap));

        String finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(responseMap)).toString();

        MicroserviceResponse registerResponse = new MicroserviceResponse();
        registerResponse.setStatus(SUCCESS_STATUS);
        registerResponse.setMessage(jwtToken);
        registerResponse.setResponse(finalResponse);

        return registerResponse;
    }

    @Override
    public MicroserviceResponse authenticateUser(String request, Map<String, String> httpHeaders) throws IOException, RouterException {
        MicroserviceResponse authenticateResponse = (MicroserviceResponse) authenticate(request, httpHeaders);

        Map<String, Object> authResponse = objectMapper.convertValue(authenticateResponse.getResponse(), new TypeReference<Map<String, Object>>() {
        });

        Object finalResponse = responseOperations.prepareResponseObject(httpHeaders.get("requestid"),
                httpHeaders.get("servicename"),
                objectMapper.writeValueAsString(authResponse));

        String token = authResponse.get("jwtToken").toString();

        MicroserviceResponse response = new MicroserviceResponse();
        response.setStatus(SUCCESS_STATUS);
        response.setMessage(token);
        response.setResponse(finalResponse);


        return response;
    }

    @Override
    public MicroserviceResponse publicRegister(String request, Map<String, String> httpHeaders) throws  IOException, RouterException {
        log.info("Generating RSA keys for public session....");
        Map<String, Object> rsaKeys = keysGenerator.generatePublicTokenAndRsaKeys(httpHeaders);

        log.info("Generating session for public session....");
        authenticationSessionService.createAndSavePublicSession(httpHeaders, rsaKeys);

        MicroserviceResponse microserviceResponse = new MicroserviceResponse();
        microserviceResponse.setStatus(SUCCESS_STATUS);
        microserviceResponse.setMessage("Keys has been generated successfully");

        microserviceResponse.setResponse(rsaKeys);

        return microserviceResponse;
    }

}
