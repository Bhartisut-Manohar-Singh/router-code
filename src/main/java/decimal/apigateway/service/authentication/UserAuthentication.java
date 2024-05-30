package decimal.apigateway.service.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.clients.UserManagerClient;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.domain.SSOTokenRedis;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.SSOTokenResponse;
import decimal.apigateway.repository.SSOTokenRepo;
import decimal.apigateway.service.authentication.sessionmgmt.AuthenticationSessionService;
import decimal.apigateway.service.util.EventRequestUtil;
import decimal.logs.masking.JsonMasker;
import lombok.extern.java.Log;
import decimal.apigateway.clients.EsbClientAuth;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class UserAuthentication {

    @Autowired
    private EsbClientAuth esbClientAuth;

    @Autowired
    private UserManagerClient userManagerClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginDetailsStorage loginDetailsStorage;

   /*     @Autowired
        AuditTraceFilter auditTraceFilter;*/

   /* @Autowired
    LogsConnector logsConnector;*/

    @Autowired
    private HttpServiceCall httpServiceCall;

    @Autowired
    private AuthenticationSessionService authenticationSessionService;

    @Autowired
    VahanaSSOService vahanaSSOService;

    @Value("${isAnalyticsRequired}")
    private String isAnalyticsRequired;

//    @Value("${raise.event.topic}")
    private String raiseEventTopic;
    @Autowired
    SSOTokenRepo ssoTokenRepo;

    @Autowired
    EventRequestUtil eventRequestUtil;

  /*  public UserAuthentication(EsbClientAuth esbClientAuth) {
        this.esbClientAuth = esbClientAuth;
    }*/

    public Object authenticate(String request, Map<String, String> httpHeaders,String loginType)
            throws RouterException, IOException {

        //logsConnector.textPayload("Preparing request to send authentication request to esb", auditTraceFilter.requestIdentifier);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode nodes = objectMapper.createObjectNode();

        //Request request1 = new Request();
        //request1.setHeaders(httpHeaders);

        String logsRequired = httpHeaders.get("logsrequired");
        String serviceLog = httpHeaders.get("servicelogs");

        boolean logRequestResponse = "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog);
        if (logRequestResponse) {
            String maskRequest = maskData(httpHeaders, request);
            //request1.setRequestBody(maskRequest);
        } else {
            nodes.put("message", "It seems that request logs is not enabled for this api/service.");
            //request1.setRequestBody(objectMapper.writeValueAsString(nodes));
        }

        String userName = httpHeaders.get(Headers.username.name());

        //logsConnector.textPayload("Executing authentication request for userName: " + userName, auditTraceFilter.requestIdentifier);

        List<String> userNameData = AuthRouterOperations.getStringArray(userName, TILD_SPLITTER);

        httpHeaders.put(Headers.orgid.name(), userNameData.get(0));
        httpHeaders.put(Headers.appid.name(), userNameData.get(1));
        httpHeaders.put(Headers.loginid.name(), userNameData.get(2));
        httpHeaders.put("Content-Type", "application/json");


        if("SSO".equalsIgnoreCase(loginType)){
            return validationSSOToken(request,httpHeaders,null,logRequestResponse,nodes);
        }

        else  {
            log.info("calling esb client************");

            httpHeaders.remove("content-length");
            Object response = esbClientAuth.executeAuthentication(request, httpHeaders);

            //logsConnector.textPayload("Response has been received from ESB", auditTraceFilter.requestIdentifier);

            Map<String, String> authResponse = objectMapper.convertValue(response, new TypeReference<Map<String, String>>() {
            });


            String responseBody = maskData(httpHeaders, objectMapper.writeValueAsString(authResponse));


            if (FAILURE_STATUS.equalsIgnoreCase(authResponse.get("status"))) {
                //logsConnector.textPayload("Failure in auth process because of: " + authResponse.get("auth"), auditTraceFilter.requestIdentifier);
                System.out.println("Failure in auth process :" + authResponse.get("auth"));
                String message = authResponse.get("message");

                RouterException exception = new RouterException(AuthRouterResponseCode.ROUTER_AUTH_FAIL, (Exception) null, "AUTH", message);
                exception.setResponse(authResponse.get("auth"));

                //VSN-3399
                exception.setErrorHint(message !=null && !message.isEmpty() ? message : "Authentication Failed");

               /* response1.setResponse(authResponse.get("auth"));
                response1.setStatus(HttpStatus.BAD_REQUEST.name());
                response1.setMessage(authResponse.get("message"));

                logsConnector.endpoint(new Payload(FAILURE_STATUS,request1, response1, auditTraceFilter.requestIdentifier));*/

               /* if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
                    EventRequest eventRequest = eventRequestUtil.setLoginLogoutDetails((objectMapper.readValue(request, ObjectNode.class)), httpHeaders, LOGIN_FAILURE_EVENT_NAME);

                    logsConnector.raiseEvent(eventRequest,raiseEventTopic);
                }*/

                throw exception;
            }

            /*logsConnector.textPayload("Authentication request to esb was successfully executed for userName: " + userName, auditTraceFilter.requestIdentifier);

            response1.setStatus(HttpStatus.OK.name());*/

            if (logRequestResponse) {
                //String maskRequest = maskData(httpHeaders, response1.getResponse());
                String maskRequest = maskData(httpHeaders, responseBody);
                //response1.setResponse(maskRequest);
            } else {
                nodes.put("message", "It seems that request logs is not enabled for this api/service.");
               // response1.setResponse(objectMapper.writeValueAsString(nodes));
            }

            //logsConnector.endpoint(new Payload(SUCCESS_STATUS,request1, response1, auditTraceFilter.requestIdentifier));

            return authResponse.get("auth");
        }
    }

    public Object authenticateV2(Object request, Map<String, String> httpHeaders,String loginType)
            throws RouterException, IOException
    {

        //logsConnector.textPayload("Preparing request to send authentication request to esb", auditTraceFilter.requestIdentifier);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode nodes = objectMapper.createObjectNode();

        //Request request1 = new Request();
        //request1.setHeaders(httpHeaders);

        String logsRequired = httpHeaders.get("logsrequired");
        String serviceLog = httpHeaders.get("servicelogs");

        boolean logRequestResponse = "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog);
        if (logRequestResponse) {
            String maskRequest = maskData(httpHeaders, (String) request);
        } else {
            nodes.put("message", "It seems that request logs is not enabled for this api/service.");
        }

        String userName = httpHeaders.get(Headers.username.name());
        List<String> userNameData = AuthRouterOperations.getStringArray(userName, TILD_SPLITTER);


        httpHeaders.put(Headers.loginid.name(), userNameData.get(2));
        httpHeaders.put("Content-Type", "application/json");

        if("SSO".equalsIgnoreCase(loginType)){
            return validationSSOToken((String) request,httpHeaders,null,logRequestResponse,nodes);
        }

        else  {
            httpHeaders.remove("content-length");
            Object response = esbClientAuth.executev2Authentication(request, httpHeaders);

           // logsConnector.textPayload("Response has been received from ESB", auditTraceFilter.requestIdentifier);

            Map<String, String> authResponse = objectMapper.convertValue(response, new TypeReference<Map<String, String>>() {
            });


            String responseBody = maskData(httpHeaders, objectMapper.writeValueAsString(authResponse));

            if (FAILURE_STATUS.equalsIgnoreCase(authResponse.get("status"))) {
             //   logsConnector.textPayload("Failure in auth process because of: " + authResponse.get("auth"), auditTraceFilter.requestIdentifier);
                System.out.println("Failure in auth process :" + authResponse.get("auth"));
                String message = authResponse.get("message");

                RouterException exception = new RouterException(AuthRouterResponseCode.ROUTER_AUTH_FAIL, (Exception) null, "AUTH", message);
                exception.setResponse(authResponse.get("auth"));

                //VSN-3399
                exception.setErrorHint(message !=null && !message.isEmpty() ? message : "Authentication Failed");

               /* response1.setResponse(authResponse.get("auth"));
                response1.setStatus(HttpStatus.BAD_REQUEST.name());
                response1.setMessage(authResponse.get("message"));

                logsConnector.endpoint(new Payload(FAILURE_STATUS,request1, response1, auditTraceFilter.requestIdentifier));
*/
                /*if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
                    EventRequest eventRequest = eventRequestUtil.setLoginLogoutDetailsV2((objectMapper.readValue(request, ObjectNode.class)), httpHeaders, LOGIN_FAILURE_EVENT_NAME);

                    logsConnector.raiseEvent(eventRequest,raiseEventTopic);
                }*/

                throw exception;
            }

           // logsConnector.textPayload("Authentication request to esb was successfully executed for userName: " + userName, auditTraceFilter.requestIdentifier);

            //response1.setStatus(HttpStatus.OK.name());

            if (logRequestResponse) {
                String maskRequest = maskData(httpHeaders, responseBody);
                //response1.setResponse(maskRequest);
            } else {
                nodes.put("message", "It seems that request logs is not enabled for this api/service.");
               // response1.setResponse(objectMapper.writeValueAsString(nodes));
            }

           // logsConnector.endpoint(new Payload(SUCCESS_STATUS,request1, response1, auditTraceFilter.requestIdentifier));

            return authResponse.get("auth");
        }
    }

    private Object validationSSOToken(String request, Map<String, String> httpHeaders, Object requestLog,boolean logRequestResponse,ObjectNode nodes) throws IOException, RouterException {
       log.info("==============================================main request============================");
       log.info(request);
        String serviceName = httpHeaders.get(Headers.servicename.name());

        JsonNode jsonNode = objectMapper.readValue(request, JsonNode.class);
        JsonNode jsonNode1 = jsonNode.get("services");
        String ssoToken= jsonNode1.get(serviceName).get(0).get("ssoToken").asText();
        String loginId= jsonNode1.get(serviceName).get(0).get("loginId").asText();
        log.info("================body=======================");
        log.info("================headers=======================");
        log.info(objectMapper.writeValueAsString(httpHeaders));
        log.info("=================sso and loginid=================");
        log.info(ssoToken);
        log.info(loginId);
        List<String> clientId = AuthRouterOperations.getStringArray(httpHeaders.get(CLIENT_ID), "~");

        String orgId = clientId.get(0);
        String appId = clientId.get(1);
        //Response responseData=new Response();
        log.info("=======================================");
        log.info("orgId" + orgId);
        log.info("appId" + appId);

        Optional<SSOTokenRedis> ssoTokenRedis=null;
        try {
            if(Strings.isBlank(loginId)){
                RouterException exception = new RouterException(AuthRouterResponseCode.ROUTER_AUTH_FAIL, (Exception) null, "AUTH", "login id can not be null or empty");
                throw  exception;
            }
             ssoTokenRedis = ssoTokenRepo.findByOrgIdAndAppIdAndLoginIdAndSsoToken(orgId,appId, loginId, ssoToken);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(!ssoTokenRedis.isPresent()){
           // logsConnector.textPayload("Failure in auth process as no SSA token found", auditTraceFilter.requestIdentifier);
            String message = "No SSA Token found for given orgId, appId and loginId";

            RouterException exception = new RouterException(AuthRouterResponseCode.ROUTER_AUTH_FAIL, (Exception) null, "AUTH", message);
            exception.setResponse(new SSOTokenResponse(FAILURE_STATUS, NO_TOKEN_FOUND, NO_TOKEN_FOUND,null));

           /* responseData.setResponse(objectMapper.writeValueAsString(exception.getResponse()));
            responseData.setStatus(HttpStatus.BAD_REQUEST.name());
            responseData.setMessage(message);*/

            //logsConnector.endpoint(new Payload(FAILURE_STATUS,requestLog, responseData, auditTraceFilter.requestIdentifier));

//            if ("Y".equalsIgnoreCase(isAnalyticsRequired))
//                httpServiceCall.callAnalyticsPortalLastLoginDetailsApi(request, httpHeaders, FAILURE_STATUS);

/*
            if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
                EventRequest eventRequest = eventRequestUtil.setLoginLogoutDetailsV2((objectMapper.readValue(request, ObjectNode.class)), httpHeaders, LOGIN_FAILURE_EVENT_NAME);
                eventRequest.setEventRemarks(SSO);
                Map<String,Object> eventData=new HashMap<>();
                eventData.put(REQUEST,jsonNode);
                eventData.put(RESPONSE,responseData);
                eventRequest.setEventData(eventData);
                logsConnector.raiseEvent(eventRequest,raiseEventTopic);
            }
*/

            throw exception;
        }
        else{
            return objectMapper.writeValueAsString(new SSOTokenResponse(SUCCESS_STATUS, TOKEN_VALID, TOKEN_VALID,null));

        }


    }

    private String maskData(Map<String, String> httpHeaders, String data) {
        String keys_to_mask = httpHeaders.get("keys_to_mask");
        if (keys_to_mask != null && !keys_to_mask.isEmpty()) {
            String[] split = keys_to_mask.split(",");
            return JsonMasker.maskMessage(data, Arrays.asList(split));
        } else {
            return data;
        }
    }

    public Object authenticateV4(String request, Map<String, String> httpHeaders,String loginType)
            throws RouterException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode nodes = objectMapper.createObjectNode();

        String logsRequired = httpHeaders.get("logsrequired");
        String serviceLog = httpHeaders.get("servicelogs");

        boolean logRequestResponse = "Y".equalsIgnoreCase(logsRequired) && "Y".equalsIgnoreCase(serviceLog);
        if (logRequestResponse) {
            String maskRequest = maskData(httpHeaders, request);
        } else {
            nodes.put("message", "It seems that request logs is not enabled for this api/service.");
        }

        String userName = httpHeaders.get(Headers.username.name());

        List<String> userNameData = AuthRouterOperations.getStringArray(userName, TILD_SPLITTER);

        httpHeaders.put(Headers.orgid.name(), userNameData.get(0));
        httpHeaders.put(Headers.appid.name(), userNameData.get(1));
        httpHeaders.put(Headers.loginid.name(), userNameData.get(2));
        httpHeaders.put("Content-Type", "application/json");

        if("SSO".equalsIgnoreCase(loginType)){
            validationSSOToken(request,httpHeaders,null,logRequestResponse,nodes);

            String serviceName = httpHeaders.get(Headers.servicename.name());
            JsonNode jsonNode = objectMapper.readValue(request, JsonNode.class);
            JsonNode jsonNode1 = jsonNode.get("services");
            String loginId= jsonNode1.get(serviceName).get(0).get("loginId").asText();

            log.info("serviceName : "+serviceName+" loginId : "+ loginId);

            Object response = userManagerClient.loginDetails(loginId);

            log.info("Response : "+objectMapper.writeValueAsString(response));
            return response;

        }

        else  {
            log.info("calling esb client************");

            Object response = esbClientAuth.executeAuthentication(request, httpHeaders);

            Map<String, String> authResponse = objectMapper.convertValue(response, new TypeReference<Map<String, String>>() {
            });

            String responseBody = maskData(httpHeaders, objectMapper.writeValueAsString(authResponse));

            if (FAILURE_STATUS.equalsIgnoreCase(authResponse.get("status"))) {
                System.out.println("Failure in auth process :" + authResponse.get("auth"));
                String message = authResponse.get("message");

                RouterException exception = new RouterException(AuthRouterResponseCode.ROUTER_AUTH_FAIL, (Exception) null, "AUTH", message);
                exception.setResponse(authResponse.get("auth"));

                //VSN-3399
                exception.setErrorHint(message !=null && !message.isEmpty() ? message : "Authentication Failed");

                throw exception;
            }

            if (logRequestResponse) {
                String maskRequest = maskData(httpHeaders, responseBody);
            } else {
                nodes.put("message", "It seems that request logs is not enabled for this api/service.");
            }

            return authResponse.get("auth");
        }
    }

}
