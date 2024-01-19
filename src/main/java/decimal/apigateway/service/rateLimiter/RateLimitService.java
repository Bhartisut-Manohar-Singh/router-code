package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.RateLimitAppAndServiceConfig;
import decimal.apigateway.entity.RateLimitEntity;
import decimal.apigateway.entity.RateLimitIpConfig;
import decimal.apigateway.repository.RateLimitAppAndServiceRepo;
import decimal.apigateway.repository.RateLimitByIpRepository;
import io.github.bucket4j.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Log
public class RateLimitService {

    @Autowired
    RateLimitByIpRepository rateLimitByIpRepository;

    @Autowired
    RateLimitAppAndServiceRepo rateLimitAppAndServiceRepo;


    public boolean allowRequestForIp(String sourceIp) {
        Optional<RateLimitIpConfig> rateLimitIpConfigOptional = rateLimitByIpRepository.findById(sourceIp);
        RateLimitEntity rateLimitEntity = rateLimitIpConfigOptional.get().getRateLimitEntity();

        long tokens = rateLimitEntity.getTokens();
        long refillInterval = rateLimitEntity.getRefillInterval();
        long bucketCapacity = rateLimitEntity.getBucketCapacity();

        // Instantiate a new bucket for each request
        Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);

        if (sourceIp.equalsIgnoreCase("abc")){
            BucketConfiguration newConfiguration = Bucket4j.configurationBuilder()
                    .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))).build();
            bucket.replaceConfiguration(newConfiguration, TokensInheritanceStrategy.AS_IS);
        }

        if (bucket.tryConsume(1)) {
            log.info("tokens left: " + bucket.getAvailableTokens());
            // Set tokens
            rateLimitEntity.setTokens(bucket.getAvailableTokens());
            rateLimitIpConfigOptional.get().setRateLimitEntity(rateLimitEntity);
            rateLimitByIpRepository.save(rateLimitIpConfigOptional.get());
            return true;
        } else {
            return false;
        }
    }



    public boolean allowRequest(String orgId,String appId,String serviceName) {
        String id = appId+serviceName;
        Optional<RateLimitAppAndServiceConfig> rateLimitConfig = rateLimitAppAndServiceRepo.findByIdAndOrgId(id,orgId);
        RateLimitEntity rateLimitEntityForApp = rateLimitConfig.get().getRateLimitEntityApp();
        RateLimitEntity rateLimitEntityForService = rateLimitConfig.get().getRateLimitEntityService();

        if(rateLimitEntityForApp!=null)
        {
            long tokens = rateLimitEntityForApp.getTokens();
            long refillInterval = rateLimitEntityForApp.getRefillInterval();
            long bucketCapacity = rateLimitEntityForApp.getBucketCapacity();

            Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);

            if (bucket.tryConsume(1)) {
                log.info("tokens left for app are : " + bucket.getAvailableTokens());
                // Set tokens
                rateLimitEntityForApp.setTokens(bucket.getAvailableTokens());
                rateLimitConfig.get().setRateLimitEntityApp(rateLimitEntityForApp);
                rateLimitAppAndServiceRepo.save(rateLimitConfig.get());

            } else {
                // no tokens left for app
                // call rejected
                return false;
            }
            // after 1 token consumption of app
        }

        // check rate limiting for service
        if(rateLimitEntityForService!=null)
        {
            long tokens = rateLimitEntityForService.getTokens();
            long refillInterval = rateLimitEntityForService.getRefillInterval();
            long bucketCapacity = rateLimitEntityForService.getBucketCapacity();

            Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);

            if (bucket.tryConsume(1)) {
                log.info("tokens left for app are : " + bucket.getAvailableTokens());
                // Set tokens
                rateLimitEntityForService.setTokens(bucket.getAvailableTokens());
                rateLimitConfig.get().setRateLimitEntityApp(rateLimitEntityForService);
                rateLimitAppAndServiceRepo.save(rateLimitConfig.get());

            } else {
                // no tokens left for service
                // call rejected
                return false;
            }
            // after 1 token consumption of service
        }else {
            return true;
        }





        return true;

    }



    private Bucket createGlobalBucket(long tokens, long refillInterval, long bucketCapacity) {
        Refill refill = Refill.intervally(tokens, Duration.ofMinutes(refillInterval));
        Bandwidth limit = Bandwidth.classic(bucketCapacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
