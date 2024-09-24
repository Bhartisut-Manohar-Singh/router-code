package decimal.apigateway.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.Headers;
import decimal.logs.model.EventRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static decimal.apigateway.commons.Constant.EVENT_SOURCE;
import static decimal.apigateway.commons.Constant.REGISTER_EVENT_NAME;
import static decimal.apigateway.commons.Constant.*;
import static decimal.apigateway.service.util.BuiltInUtility.*;


@Service
@Log
public class EventRequestUtil {

    @Autowired
    ObjectMapper objectMapper;



    public EventRequest setRegisterDetails(JsonNode request, Map<String, String> httpHeaders) throws JsonProcessingException {

        log.info("------------------Inside Event --------------------------------------");
        EventRequest eventRequest=new EventRequest();

        String[] orgAndApp = httpHeaders.containsKey("clientid") ? httpHeaders.get("clientid").split("~") : new String[]{"", ""};

        eventRequest.setOrgId(orgAndApp[0]);
        eventRequest.setAppId(orgAndApp[1]);
        eventRequest.setEventName(REGISTER_EVENT_NAME);
        eventRequest.setEventSource(EVENT_SOURCE);

        setEventFromHeaders(eventRequest, httpHeaders,request);

        //eventRequest.setArchivalDays(archivedDays);
       // eventRequest.setPurgeDays(purgeDays);

        eventRequest.setExecutionTime(getFormattedCurrentTimestamp());

        return eventRequest;

    }


    public EventRequest setLoginLogoutDetails(JsonNode request, Map<String, String> httpHeaders, String eventName) throws JsonProcessingException {
        log.info("-----------------------------------Inside Login-----------------------------");
        EventRequest eventRequest=new EventRequest();


        setEventFromHeaders(eventRequest, httpHeaders, request);

        String userName = httpHeaders.get(Headers.username.name());
        List<String> userNameData = AuthRouterOperations.getStringArray(userName, TILD_SPLITTER);

        eventRequest.setEventName(eventName);
        eventRequest.setEventSource(EVENT_SOURCE);

        eventRequest.setOrgId(userNameData.get(0));
        eventRequest.setAppId(userNameData.get(1));
        eventRequest.setLoginId(userNameData.get(2));
        eventRequest.setRaisedBy(userNameData.get(2));

        eventRequest.setExecutionTime(getFormattedCurrentTimestamp());

        return eventRequest;
    }

    public EventRequest setLoginLogoutDetailsV2(JsonNode request, Map<String, String> httpHeaders, String eventName) throws JsonProcessingException {
        log.info("-----------------------------------Inside Login-----------------------------");
        EventRequest eventRequest=new EventRequest();


        setEventFromHeaders(eventRequest, httpHeaders, request);

        String userName = httpHeaders.get(Headers.username.name());
        List<String> userNameData = AuthRouterOperations.getStringArray(userName, Constant.TILD_SPLITTER);

        eventRequest.setEventName(eventName);
        eventRequest.setEventSource(EVENT_SOURCE);

        eventRequest.setOrgId(httpHeaders.get(Headers.orgid.name()));
        eventRequest.setAppId(httpHeaders.get(Headers.appid.name()));
        eventRequest.setLoginId(httpHeaders.get(Headers.loginid.name()));
        eventRequest.setRaisedBy(httpHeaders.get(Headers.loginid.name()));

        eventRequest.setExecutionTime(getFormattedCurrentTimestamp());

        return eventRequest;
    }

    private void setEventFromHeaders(EventRequest eventRequest, Map<String, String> httpHeaders, JsonNode request) throws JsonProcessingException {

        if(request!=null) {

            JsonNode interfaces = request.get("interfaces");

            if (interfaces != null) {
                httpHeaders.put("applicationversion", interfaces.has("APPLICATION_VERSION") ? interfaces.get("APPLICATION_VERSION").asText(): "");

                httpHeaders.put("devicelatitude", interfaces.has("DEVICE_LATITUDE") ? interfaces.get("DEVICE_LATITUDE").asText() : "");

                httpHeaders.put("devicelongitude", interfaces.has("DEVICE_LONGITUDE") ? interfaces.get("DEVICE_LONGITUDE").asText() : "");

                httpHeaders.put("imeino", interfaces.has("IMEI_NO") ? interfaces.get("IMEI_NO").asText() : "");

                httpHeaders.put("devicemake",interfaces.has("DEVICE_MAKE") ? interfaces.get("DEVICE_MAKE").asText() : "");

                httpHeaders.put("devicemodel",interfaces.has("DEVICE_MODEL") ? interfaces.get("DEVICE_MODEL").asText() : "");

                httpHeaders.put("osversion",interfaces.has("OS_VERSION") ? interfaces.get("OS_VERSION").asText() : "");

            }
        }

        eventRequest.setRequestHeaders(objectMapper.writeValueAsString(httpHeaders));

        eventRequest.setObjectPriKey1(httpHeaders.containsKey("clientid") ? httpHeaders.get("clientid") : "");

    }

}
