package decimal.apigateway.entity;

import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitEntity {
    long time;
    TimeUnit unit;
    long noOfAllowedHits;
    String rateLimitLevel;
    String bucket;



}
