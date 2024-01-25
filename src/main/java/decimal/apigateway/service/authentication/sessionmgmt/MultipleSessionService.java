package decimal.apigateway.service.authentication.sessionmgmt;

import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.AuthRouterResponseCode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.domain.Session;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.model.ApplicationDef;
import decimal.apigateway.service.AuthApplicationDefConfig;
import decimal.logs.filters.AuditTraceFilter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author ashish.kumar
 */
@Service
@Log
public class MultipleSessionService implements MultipleSession {
    @Autowired
    AuthenticationSessionService authenticationSessionService;

    @Autowired
    AuthApplicationDefConfig applicationDefConfig;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    

    @Override
    public void validateMultipleSession(Map<String, String> httpHeaders) throws RouterException {
        String username = httpHeaders.get(Headers.username.name());
        String requestId = httpHeaders.get(Headers.requestid.name());

        List<String> userNameData = AuthRouterOperations.getStringArray(username, Constant.TILD_SPLITTER);

        String orgId = userNameData.get(0);

        String appId = userNameData.get(1);

        String loginId = userNameData.get(2);

        String deviceId = userNameData.get(3);

        log.info("Validating multiple session for usrName: " + username + " and requestId: " + requestId);

        ApplicationDef application = applicationDefConfig.findByOrgIdAndAppId(orgId, appId);

        boolean isMultipleSessionAllowed = "Y".equalsIgnoreCase(application.getIsMultipleSessionAllowed());

        if (!isMultipleSessionAllowed) {
            log.info("Multiple session is not allowed for username" + username);
            authenticationSessionService.removeSessionByOrgIdAndAppIdAndDeviceId(orgId, appId, deviceId);
            List<Session> sessions = authenticationSessionService.findByOrgIdAndAppIdAndLoginId(orgId, appId, loginId);

            manageForceLoginSessions(sessions, httpHeaders.get(Constant.IS_FORCE_LOGIN), username, requestId);
        }

    }

    @Override
    public boolean killAllUserSessions(String orgId, String appId, String loginId) {
        List<Session> allSessions;

        if ("*".equalsIgnoreCase(loginId)) {
            allSessions = authenticationSessionService.findByOrgIdAndAppId(orgId, appId);
        } else {
            allSessions = authenticationSessionService.findByOrgIdAndAppIdAndLoginId(
                    orgId, appId, loginId);
        }

        if (allSessions.isEmpty()) {
            log.info("No session found for userKey: " + orgId + appId + loginId);
            return false;
        }

        log.info("No. of session found for key: " + orgId + appId + loginId + " is " + allSessions.size());

        authenticationSessionService.removeAllSessions(allSessions);

        log.info("Multiple Session are killed for " + orgId + ":" + appId + ":" + loginId);

        return true;
    }

    @Override
    public boolean killAllAppSessions(String orgId, String appId, String deviceId) {
        List<Session> allSessions = authenticationSessionService.findByOrgIdAndAppIdAndDeviceId(
                orgId, appId, deviceId);

        if (allSessions.isEmpty()) {
            log.info("No session found for appKey: " + orgId + appId + deviceId);
            return false;
        }

        log.info("No. of session found for key: " + orgId + appId + deviceId + " is " + allSessions.size());

        authenticationSessionService.removeAllSessions(allSessions);

        log.info("Multiple Session are killed for " + orgId + ":" + appId + ":" + deviceId);

        return true;
    }

    private void manageForceLoginSessions(List<Session> sessions, String isForceLogin, String userName, String requestId) throws RouterException {

        if (!Objects.equals("Y", isForceLogin) && !sessions.isEmpty()) {
            log.info("Multiple Session are not allowed, session exists for user:" + userName
                    + ". So user is not allowed to create a new session:" + requestId);
            throw new RouterException(AuthRouterResponseCode.ROUTER_MULTIPLE_SESSION, (Exception) null, Constant.ROUTER_ERROR_TYPE_SECURITY, "User is not allowed to create multiple session because of no multiple session is allowed");
        }

        authenticationSessionService.removeAllSessions(sessions);

        log.info(
                "Multiple Session are not allowed, Force Login is True, " + sessions.size()
                        + " existing session killed for user:" + userName + ":" + requestId);
    }
}
