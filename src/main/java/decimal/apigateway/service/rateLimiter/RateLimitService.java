package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import io.github.bucket4j.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;


    public boolean allowRequest(String appId, String serviceName) {
        Bucket bucketForApp;
        Bucket bucketForService;

        // checks in redis if config is present
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
        RateLimitEntity rateLimitAppEntity;
        RateLimitEntity rateLimitServiceEntity;


        if(rateLimitAppConfig.isPresent())
        {
            rateLimitAppEntity = rateLimitAppConfig.get().getRateLimitEntity();
            bucketForApp = getOrCreateBucket(appId, rateLimitAppEntity).getBucket();

            if (!consumeTokensForApp(bucketForApp, rateLimitAppConfig.get())) {
                return false;
            }

        }else{
            return false;}

        if(rateLimitServiceConfig.isPresent()){
            rateLimitServiceEntity = rateLimitServiceConfig.get().getRateLimitEntity();
            bucketForService = getOrCreateBucket(appId + "+" + serviceName, rateLimitServiceEntity).getBucket();

            if (!consumeTokensForService(bucketForService,rateLimitServiceConfig.get())) {
                return false;
            }

        }else{
            return false;}
        // Both app and service checks passed
        return true;
    }



    RateLimitEntity getOrCreateBucket(String id, RateLimitEntity rateLimitEntity) {
        if(rateLimitEntity.getBucket()!=null)
            return rateLimitEntity;
        else{
            return createAndSetBucket(id,rateLimitEntity);
        }
    }

    boolean consumeTokensForApp(Bucket bucket,RateLimitAppConfig rateLimitAppConfig) {
        if (bucket.tryConsume(1)) {
            rateLimitAppConfig.getRateLimitEntity().setBucket(bucket);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("Tokens left are "+bucket.getAvailableTokens());
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }

    boolean consumeTokensForService(Bucket bucket,RateLimitServiceConfig rateLimitServiceConfig) {
        if (bucket.tryConsume(1)) {
            rateLimitServiceConfig.getRateLimitEntity().setBucket(bucket);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            log.info("Tokens left are "+bucket.getAvailableTokens());
            log.info(bucket.toString());
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }


    RateLimitEntity createAndSetBucket(String id, RateLimitEntity rateLimitEntity) {
        Bucket newbucket = createBucket(rateLimitEntity);
        rateLimitEntity.setBucket(newbucket);
        return rateLimitEntity;
    }


    Bucket createBucket(RateLimitEntity rateLimitEntity) {
        long capacity = rateLimitEntity.getNoOfAllowedHits();
        long duration = rateLimitEntity.getTime();
        TimeUnit timeUnit = rateLimitEntity.getUnit();
        return Bucket.builder().addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration))))).build();
    }


//    public Bucket resolveBucket(String key) {
//        Supplier<BucketConfiguration> configSupplier = getConfigSupplierForUser(key);
//
//        // Does not always create a new bucket, but instead returns the existing one if it exists.
//        return buckets.builder().build(key, configSupplier);
//    }
//    private Supplier<BucketConfiguration> getConfigSupplierForUser(String key) {
//
//        Refill refill = Refill.intervally(20, Duration.ofMinutes(1));
//        Bandwidth limit = Bandwidth.classic(20, refill);
//        return () -> (BucketConfiguration.builder()
//                .addLimit(limit)
//                .build());
//    }

}
