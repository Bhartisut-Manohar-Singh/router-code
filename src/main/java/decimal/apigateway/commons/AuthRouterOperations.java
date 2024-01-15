package decimal.apigateway.commons;

import decimal.apigateway.exception.RouterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static decimal.apigateway.loggers.Loggers.ERROR_LOGGER;

@Component
public class AuthRouterOperations {

    private static final Logger logger = LogManager.getLogger(AuthRouterOperations.class);

    private AuthRouterOperations() {

    }

    public static List<String> getStringArray(String data, String spliterFrmt) {
        return Arrays.asList(data.split(spliterFrmt));
    }

    public static long getSessionExpiryInMinutes(long userSessionExpiryTime) {
        return userSessionExpiryTime * 60 * 1000;
    }

    public static long getSessionExpiryInDays(long appSessionExpiry) {
        return appSessionExpiry * 60 * 1000 * 60 * 24;
    }

    public static void validateSessionParameters(String... args ) throws RouterException {

        for (String ar : args)
        {
            if(ar == null || ar.isEmpty())
            {
                ERROR_LOGGER.error("OrgId can not be null/empty in application session creation.");
                throw new RouterException(AuthRouterResponseCode.INVALID_SESSION_PARAMETERS, (Exception) null, ConstantsAuth.ROUTER_ERROR_TYPE_VALIDATION, "Org ID is found null or empty for request");
            }
        }
    }

    public static String getLogMessage(String requestId, String message) {

        return requestId + " - " + message;
    }
}
