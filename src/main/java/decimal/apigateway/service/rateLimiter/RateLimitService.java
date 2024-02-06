package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.repository.BucketRepo;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitByIpRepository;
import decimal.apigateway.repository.RateLimitServiceRepo;
import io.github.bucket4j.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;

    @Autowired
    BucketRepo bucketRepo;



    public boolean allowRequest(String appId, String serviceName) {
        Bucket bucketForApp;
        Bucket bucketForService;

        // checks in redis if config is present
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
        if(rateLimitAppConfig.isPresent())
        { //checks if bucket is present
            bucketForApp = checkBucket(appId,rateLimitAppConfig.get().getRateLimitEntityApp());
        }else{
            return false;}

        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
        if(rateLimitServiceConfig.isPresent())
        { //checks if bucket is present
            bucketForService = checkBucket(appId + "+" + serviceName,rateLimitServiceConfig.get().getRateLimitEntityService());
        }else{
            return false;}


        // Check rate limiting for app
        if (bucketForApp.tryConsume(1)) {
            bucketRepo.save(new BucketConfig(appId,bucketForApp));
            log.info("Tokens left for app are: " + bucketForApp.getAvailableTokens());
        } else {
            // No tokens left for app
            return false;
        }

        // Check rate limiting for service
        if (bucketForService.tryConsume(1)) {
            bucketRepo.save(new BucketConfig(appId+"+"+serviceName,bucketForService));
            log.info("Tokens left for service are: " + bucketForService.getAvailableTokens());
        } else {
            // No tokens left for service
            return false;
        }

        // Both app and service checks passed
        return true;
    }


    private  Bucket checkBucket(String id, RateLimitEntity rateLimitEntity){
        Optional<BucketConfig> bucketConfig = bucketRepo.findById(id);
        if(bucketConfig.isEmpty()){
//            Bucket newBucket = createBucket(rateLimitEntity.getNoOfAllowedHits(),rateLimitEntity.getTime(),rateLimitEntity.getUnit());
//            bucketRepo.save(new BucketConfig(id,newBucket));
            return createBucket(rateLimitEntity.getNoOfAllowedHits(),rateLimitEntity.getTime(),rateLimitEntity.getUnit());
        }
        return bucketConfig.get().getBucket();
    }


    public Bucket createBucket(long capacity, long duration, TimeUnit timeUnit) {
        Refill refill = Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration)));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }





}
