package decimal.apigateway.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RouterOperations {

    private static final Logger logger = LogManager.getLogger(RouterOperations.class);



    public static List<String> getStringArray(String data, String spliterFrmt) {
        return Arrays.asList(data.split(spliterFrmt));
    }

    public static long getSessionExpiryInMinutes(long userSessionExpiryTime) {
        return userSessionExpiryTime * 60 * 1000;
    }

    public static long getSessionExpiryInDays(long appSessionExpiry) {
        return appSessionExpiry * 60 * 1000 * 60 * 24;
    }




}
