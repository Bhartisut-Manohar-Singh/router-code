package decimal.apigateway.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BucketConfig {
    long time;
    String unit;
    int noOfAllowedHits;
    String rateLimitLevel;
}
