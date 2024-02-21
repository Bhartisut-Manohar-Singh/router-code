package decimal.apigateway.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RATE_LIMIT_CONFIG")
public class RateLimitConfig{
    @Id
    String id;
    String orgId;
    String appId;
    String apiName;
    long duration;
    TimeUnit durationUnit;
    long maxAllowedHits;
    String level;
}
