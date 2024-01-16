package decimal.apigateway.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitEntity {
    long tokens;
    long refillInterval;
    long bucketCapacity;


}
