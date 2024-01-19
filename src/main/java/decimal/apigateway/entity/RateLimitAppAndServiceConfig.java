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
@RedisHash("RATE_LIMITER_APP_AND_SERVICE")
public class RateLimitAppAndServiceConfig {
    @Id
    String id;
    String orgId;
    String appId;
    String serviceName;
    RateLimitEntity rateLimitEntityApp;
    RateLimitEntity rateLimitEntityService;

}