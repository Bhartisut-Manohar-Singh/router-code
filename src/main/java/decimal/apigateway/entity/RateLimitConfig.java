package decimal.apigateway.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RATE_LIMITER_CONFIG")
public class RateLimitConfig{
    @Id
    String id;
    String orgId;
    long time;
    String unit;
    int noOfAllowedHits;
    String rateLimitLevel;
}
