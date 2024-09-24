/**
 * 
 */
package decimal.apigateway.service.authentication.sessionmgmt;

import decimal.apigateway.exception.RouterException;

import java.util.Map;

/**
 * @author vikas
 *
 */
public interface MultipleSession {

    void validateMultipleSession(Map<String, String> httpHeaders) throws RouterException;

    boolean killAllUserSessions(String orgId, String appId, String loginId);

    boolean killAllAppSessions(String orgId, String appId, String deviceId);
}
