package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.RateLimitAppConfig;
import decimal.apigateway.entity.RateLimitServiceConfig;
import decimal.apigateway.entity.RateLimitEntity;
import decimal.apigateway.entity.RateLimitIpConfig;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitByIpRepository;
import decimal.apigateway.repository.RateLimitServiceRepo;
import io.github.bucket4j.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Log
public class RateLimitService {
    @Autowired
    private RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitByIpRepository rateLimitByIpRepository;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;

    private final Map<String, Bucket> appBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> serviceBuckets = new ConcurrentHashMap<>();


//
//    private final Bucket bucketForApp;
//    private final Bucket bucketForService;
//
//    public RateLimitService() {
//        Bandwidth limit = Bandwidth.classic(5,Refill.intervally(5, Duration.ofMinutes(1)));
//        this.bucketForApp = Bucket.builder()
//                .addLimit(limit)
//                .build();
//
//        Bandwidth limit2 = Bandwidth.classic(2,Refill.intervally(2, Duration.ofMinutes(1)));
//        this.bucketForService = Bucket.builder()
//                .addLimit(limit2)
//                .build();
//    }



//    public boolean allowRequestForIp(String sourceIp) {
//        Optional<RateLimitIpConfig> rateLimitIpConfigOptional = rateLimitByIpRepository.findById(sourceIp);
//        RateLimitEntity rateLimitEntity = rateLimitIpConfigOptional.get().getRateLimitEntity();
//
//        long tokens = rateLimitEntity.getTokens();
//        long refillInterval = rateLimitEntity.getRefillInterval();
//        long bucketCapacity = rateLimitEntity.getBucketCapacity();
//
//        // Instantiate a new bucket for each request
//        Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);
//
//        if (sourceIp.equalsIgnoreCase("abc")){
//            BucketConfiguration newConfiguration = Bucket4j.configurationBuilder()
//                    .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))).build();
//            bucket.replaceConfiguration(newConfiguration, TokensInheritanceStrategy.AS_IS);
//        }
//
//        if (bucket.tryConsume(1)) {
//            log.info("tokens left: " + bucket.getAvailableTokens());
//            // Set tokens
//            rateLimitEntity.setTokens(bucket.getAvailableTokens());
//            rateLimitIpConfigOptional.get().setRateLimitEntity(rateLimitEntity);
//            rateLimitByIpRepository.save(rateLimitIpConfigOptional.get());
//            return true;
//        } else {
//            return false;
//        }
//    }



//    public boolean allowRequest(String orgId,String appId,String serviceName) {
//        String id = appId+serviceName;
//        Optional<RateLimitServiceConfig> rateLimitConfig = rateLimitAppAndServiceRepo.findByIdAndOrgId(id,orgId);
//        RateLimitEntity rateLimitEntityForApp = rateLimitConfig.get().getRateLimitEntityApp();
//        RateLimitEntity rateLimitEntityForService = rateLimitConfig.get().getRateLimitEntityService();
//
//        if(rateLimitEntityForApp!=null)
//        {
//            long tokens = rateLimitEntityForApp.getTokens();
//            long refillInterval = rateLimitEntityForApp.getRefillInterval();
//            long bucketCapacity = rateLimitEntityForApp.getBucketCapacity();
//
//            Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);
////            this.bucket1 = Bucket.builder().build();
//
//            if (bucket.tryConsume(1)) {
//                log.info("tokens left for app are : " + bucket.getAvailableTokens());
//                // Set tokens
//                rateLimitEntityForApp.setTokens(bucket.getAvailableTokens());
//                rateLimitConfig.get().setRateLimitEntityApp(rateLimitEntityForApp);
//                rateLimitAppAndServiceRepo.save(rateLimitConfig.get());
//
//            } else {
//                // no tokens left for app
//                // call rejected
//                return false;
//            }
//            // after 1 token consumption of app
//        }
//
//        // check rate limiting for service
//        if(rateLimitEntityForService!=null)
//        {
//            long tokens = rateLimitEntityForService.getTokens();
//            long refillInterval = rateLimitEntityForService.getRefillInterval();
//            long bucketCapacity = rateLimitEntityForService.getBucketCapacity();
//
//            Bucket bucket = createGlobalBucket(tokens, refillInterval, bucketCapacity);
//
//            if (bucket.tryConsume(1)) {
//                log.info("tokens left for app are : " + bucket.getAvailableTokens());
//                // Set tokens
//                rateLimitEntityForService.setTokens(bucket.getAvailableTokens());
//                rateLimitConfig.get().setRateLimitEntityApp(rateLimitEntityForService);
//                rateLimitAppAndServiceRepo.save(rateLimitConfig.get());
//
//            } else {
//                // no tokens left for service
//                // call rejected
//                return false;
//            }
//            // after 1 token consumption of service
//        }else {
//            return true;
//        }
//
//
//
//
//
//        return true;
//
//    }


    public boolean allowRequest22(String orgId, String appId, String serviceName) {
        // Retrieve bucket for app
        Bucket bucketForApp = appBuckets.computeIfPresent(appId, (key, existingBucket) -> existingBucket);
        if (bucketForApp==null) {
            bucketForApp = createBucketForApp(orgId, appId);
            if(bucketForApp==null){
                return false;
            }



        }
        // Retrieve bucket for service
        Bucket bucketForService = serviceBuckets.computeIfPresent(appId+"+"+serviceName, (key, existingBucket) -> existingBucket);
        if (bucketForService==null) {
            bucketForService = createBucketForService(orgId, appId,serviceName);
            if(bucketForService==null){
                return false;
            }

        }

        // Check rate limiting for app
        if (bucketForApp.tryConsume(1)) {
            log.info("Tokens left for app are: " + bucketForApp.getAvailableTokens());
        } else {
            // No tokens left for app
            // Call rejected
            return false;
        }

        // Check rate limiting for service
        if (bucketForService.tryConsume(1)) {
            log.info("Tokens left for service are: " + bucketForService.getAvailableTokens());
        } else {
            // No tokens left for service
            // Call rejected
            return false;
        }

        // Both app and service checks passed
        return true;
    }

    private Bucket createBucketForApp(String orgId, String appId) {
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
        if (rateLimitAppConfig.isPresent()) {
            RateLimitEntity rateLimitEntityApp = rateLimitAppConfig.get().getRateLimitEntityApp();
            Bucket newBucket = createGlobalBucket(rateLimitEntityApp.getNoOfAllowedHits(), rateLimitEntityApp.getTime(), rateLimitEntityApp.getUnit());
            appBuckets.put(appId, newBucket);
            return newBucket;
        }
        // Return a default bucket if configuration not found
        return null;
    }

    private Bucket createBucketForService(String orgId, String appId, String serviceName) {
        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
        if (rateLimitServiceConfig.isPresent()) {
            RateLimitEntity rateLimitEntityService = rateLimitServiceConfig.get().getRateLimitEntityService();
            Bucket newBucket = createGlobalBucket(rateLimitEntityService.getNoOfAllowedHits(), rateLimitEntityService.getTime(), rateLimitEntityService.getUnit());
            serviceBuckets.put(appId+"+"+serviceName,newBucket);
            return newBucket;
        }
        // Return a default bucket if configuration not found
        return null;
    }

    public Bucket createBucket(long capacity, long refillInterval, TimeUnit refillIntervalUnit) {
        Refill refill = Refill.intervally(capacity, Duration.ofMillis(refillIntervalUnit.toMillis(refillInterval)));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGlobalBucket(long capacity, long duration, TimeUnit timeUnit) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration)))))
                .build();
    }



    private Bucket createGlobalBucket(long tokens, long refillInterval, long bucketCapacity) {
        Refill refill = Refill.intervally(tokens, Duration.ofMinutes(refillInterval));
        Bandwidth limit = Bandwidth.classic(bucketCapacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

}
