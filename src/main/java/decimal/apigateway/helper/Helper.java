package decimal.apigateway.helper;

import java.time.Duration;


public class Helper {
    public Duration findDuration(long durationValue, String unit){
        Duration duration = null;
        switch (unit) {
            case "SECONDS":
                duration = Duration.ofSeconds(durationValue);
                break;
            case "MINUTES":
                duration = Duration.ofMinutes(durationValue);
                break;
            case "HOURS":
                duration = Duration.ofHours(durationValue);
                break;
            case "DAYS":
                duration = Duration.ofDays(durationValue);
                break;
            default:
                System.out.println("Invalid duration unit. Exiting.");
                
        }
        return duration;
    }
}
