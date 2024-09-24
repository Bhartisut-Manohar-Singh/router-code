package decimal.apigateway.commons;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import decimal.apigateway.domain.Session;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

@Component
public class RouterOperations {

    private RouterOperations() {

    }

    public static List<String> getStringArray(String data, String spliterFrmt) {
        return Arrays.asList(data.split(spliterFrmt));
    }

    public static JsonObject getJsonObject(Object obj) {
        JsonParser parser = new JsonParser();
        return parser.parse(obj.toString()).getAsJsonObject();

    }
    public static String getLogMessage(String requestId, String message) {

        return requestId + " - " + message;
    }

    /*Function to convert Hex to String */
    public static String hexToString(String hexStr) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));

        }
        return output.toString();
    }

    public static JsonObject fetchRSADatafromSession(Session session) {
        JsonParser parser = new JsonParser();
        return parser.parse(session.getSessionData().get("rsa")).getAsJsonObject();

    }

    public static String getJoiningString(Object separator, String... args) {

        StringJoiner joiner = new StringJoiner(separator.toString());

        for (String arg : args)
        {
            joiner.add(arg);
        }

        return joiner.toString();
    }

    public static String getJoiningString(String... args) {

        StringJoiner joiner = new StringJoiner("-", "[", "]");

        for (String arg : args)
        {
            joiner.add(arg);
        }

        return joiner.toString();
    }
}
