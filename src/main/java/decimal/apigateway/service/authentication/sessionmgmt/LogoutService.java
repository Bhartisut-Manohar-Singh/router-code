package decimal.apigateway.service.authentication.sessionmgmt;


import com.fasterxml.jackson.core.JsonProcessingException;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.service.util.EventRequestUtil;
import decimal.apigateway.service.authentication.HttpServiceCall;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.EventRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static decimal.apigateway.commons.Constant.LOGOUT_EVENT_NAME;
import static decimal.apigateway.commons.Constant.ROUTER_ERROR_TYPE_SECURITY;

@Service
@Log
public class LogoutService {

    

    @Autowired
    AuthenticationSessionService authenticationSessionService;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    @Autowired
    private HttpServiceCall httpServiceCall;

    @Value("${isAnalyticsRequired}")
    private String isAnalyticsRequired;

    @Autowired
    EventRequestUtil eventRequestUtil;


    public void doLogout(Map<String, String> httpHeaders) throws RouterException, JsonProcessingException {
        String username = httpHeaders.get(Constant.ROUTER_HEADER_USERNAME);

        log.info("Username trying to logout -- " + username);

        log.info("Processing logout request" +auditTraceFilter.requestIdentifier);
        log.info("Killing session for: " + username +
                auditTraceFilter.requestIdentifier);

        try {
            log.info("Processing logout request");

            log.info("Killing session for: " + username );

            if ("Y".equalsIgnoreCase(isAnalyticsRequired) && username!=null && !username.isEmpty()){
                EventRequest eventRequest=eventRequestUtil.setLoginLogoutDetails(null,httpHeaders, LOGOUT_EVENT_NAME);

                //logsConnector.raiseEvent(eventRequest,raiseEventTopic);
            }

            authenticationSessionService.removeSession(username);

        } catch (Exception e) {
            log.info("Error when performing logout request for userName: " + username);

            if ("Y".equalsIgnoreCase(isAnalyticsRequired)){
                EventRequest eventRequest=eventRequestUtil.setLoginLogoutDetails(null,httpHeaders, LOGOUT_EVENT_NAME);

                //logsConnector.raiseEvent(eventRequest,raiseEventTopic);
            }

            throw new RouterException(AuthRouterResponseCode.ROUTER_KILL_SESSION_FAIL, e, ROUTER_ERROR_TYPE_SECURITY, null);
        }
        log.info("User has been logout successfully");

    }
}
