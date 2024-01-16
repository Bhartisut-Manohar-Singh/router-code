package decimal.apigateway.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("RATE_LIMIT_IP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitIpConfig {
    @Id
    String sourceIp;
    RateLimitEntity rateLimitEntity;
}
