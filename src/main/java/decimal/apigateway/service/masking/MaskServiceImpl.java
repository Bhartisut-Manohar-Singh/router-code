package decimal.apigateway.service.masking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MaskServiceImpl implements MaskService {

    @Value("${keys_to_mask}")
    String keyToMask;

    public  String maskMessage(String message)  {

        String maskedMessage = message;

        if(keyToMask == null || keyToMask.isEmpty())
            return maskedMessage;

        String[] keysToMask = keyToMask.split(",");

        for (String keyToMask : keysToMask) {
            maskedMessage = maskJsonData(maskedMessage, keyToMask);
        }

        return maskedMessage;
    }

    private static String maskJsonData(String message, String keyToMask) {

        String maskedMessage = message;

        try {
            if (message != null) {

                for (String getRegex : getRegexPattern(keyToMask)) {

                    Pattern pattern = Pattern.compile(getRegex,
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(message);
                    while (matcher.find()) {
                        String keyName = matcher.group(1).replaceAll("\"", "");
                        String maskMessage = matcher.group(3);

                        if (maskMessage != null && keyToMask.equalsIgnoreCase(keyName) && !maskMessage.isEmpty()
                                && !maskMessage.equalsIgnoreCase("\"\""))
                            maskedMessage = maskedMessage.replace(maskMessage, "\"*****\"");
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maskedMessage;
    }


    private static List<String> getRegexPattern(String key) {
        List<String> regex = new ArrayList<>();
        try {
            String regex2 = "(" + "\"" + key + "\"" + ")(\\s*+:\\s*+)((\"\"|\".+?\"))";

            regex.add(regex2);
//            regex.add("(.+?)(?:,|$)(" + key + ")(\\s*=\\s*)(.+?)[\\s{ 0 , }?]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regex;
    }

}
