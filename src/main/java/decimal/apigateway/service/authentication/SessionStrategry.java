package decimal.apigateway.service.authentication;



import decimal.apigateway.domain.Session;
import decimal.apigateway.exception.RouterException;

import java.util.Map;
import java.util.Set;

public interface SessionStrategry {

    Session isValidSession(String requestId, String key);

    void checkInactiveSession(String token, String requestId, int inactiveSessionExpTimeMinutes) throws RouterException;

    void storeInactiveSessionDetails(String token, String requestId, Long inactiveSessionExpTimeMinutes) throws RouterException;


    boolean removeSession(String key) throws RouterException;

    Set<String> getKeys();

    void createUserSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException;

    void createAndSaveAppSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException;

    void createAndSavePublicSession(Map<String, String> httpHeaders, Map<String, Object> rsaKeys) throws RouterException;

}
