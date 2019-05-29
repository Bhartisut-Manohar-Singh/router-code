package decimal.apigateway.service.masking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
@Service
public class MaskServiceImpl implements MaskService {

    @Value("#{'${keys_to_mask}'.split(',')}")
    List<String> keysToMask;

    @Override
    public String maskMessage(String message) {

        String maskedMessage = message;

        if (keysToMask != null) {
            for (String keyToMask : keysToMask) {
                maskedMessage = maskJsonData(maskedMessage, keyToMask);
            }
        }

        return maskedMessage;
    }

    private String maskJsonData(String message, String keyToMask) {

        String maskedMessage = message;
        if (message != null) {
            for (String getRegex : getRegexPattern(keyToMask)) {

                Pattern pattern = Pattern.compile(getRegex, Pattern.CASE_INSENSITIVE);

                Matcher matcher = pattern.matcher(message);

                while (matcher.find()) {
                    String maskMessage = matcher.group(3);
                    maskedMessage = maskedMessage.replaceFirst(maskMessage, "\"*****\"");
                }

            }
        }

        return maskedMessage;

    }

    private static List<String> getRegexPattern(String key) {
        List<String> regex = new ArrayList<>();
        regex.add("(" + "\"" + key + "\"" + ")(\\s*+:\\s*+)(\".+?\")");
        regex.add("(" + key + ")(\\s*=\\s*)(.+?)[\\s{ 0 , }?]");

        return regex;
    }

}
