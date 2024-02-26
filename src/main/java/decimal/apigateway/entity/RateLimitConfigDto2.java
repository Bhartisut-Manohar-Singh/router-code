package decimal.apigateway.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class RateLimitConfigDto2 {
    String orgId;
    String appId;
    String serviceName;
    long time;
    TimeUnit unit;
    Long noOfAllowedHits;
    String rateLimitLevel;
}
