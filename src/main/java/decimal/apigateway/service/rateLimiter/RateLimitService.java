package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.RateLimitIpConfig;
import decimal.apigateway.repository.RateLimitByIpRepository;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log
public class RateLimitService {

    @Autowired
    RateLimitByIpRepository rateLimitByIpRepository;
    private static Bucket globalBucket;

    public boolean allowRequest(String sourceIp) {
        RateLimitIpConfig rateLimitIpConfig = rateLimitByIpRepository.findBySourceIp(sourceIp);

        long tokens = rateLimitIpConfig.getRateLimitEntity().getTokens();
        long refillInterval = rateLimitIpConfig.getRateLimitEntity().getRefillInterval();
        long bucketCapacity = rateLimitIpConfig.getRateLimitEntity().getBucketCapacity();

        this.globalBucket = createGlobalBucket(tokens,refillInterval,bucketCapacity);

        if (globalBucket.tryConsume(1)) {
            log.info("tokens left: " + globalBucket.getAvailableTokens());
            //set tokens
            rateLimitIpConfig.getRateLimitEntity().setTokens(globalBucket.getAvailableTokens());
            rateLimitByIpRepository.save(rateLimitIpConfig);
            return true;
        } else {
            return false;
        }
    }

    private Bucket createGlobalBucket(long tokens, long refillInterval, long bucketCapacity) {

        Refill refill = Refill.intervally(tokens, Duration.ofMinutes(refillInterval));
        Bandwidth limit = Bandwidth.classic(bucketCapacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
