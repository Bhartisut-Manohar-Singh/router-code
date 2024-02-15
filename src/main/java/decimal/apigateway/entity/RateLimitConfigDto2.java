package decimal.apigateway.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitConfigDto2 {
    String orgId;
    String appId;
    String serviceName;
    BucketConfig appBucketConfig;
    BucketConfig serviceBucketConfig;
}
