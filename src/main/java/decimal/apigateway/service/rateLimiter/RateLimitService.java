package decimal.apigateway.service.rateLimiter;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import io.github.bucket4j.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProxyManagerConfiguration proxyConfiguration;

    private final Class<E> extension;
    private ProxyManager<String> buckets;

    public boolean allowRequest(String appId, String serviceName) throws IOException {
//        MyBucket bucketForApp;
//        MyBucket bucketForService;



        //test
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMillis(100)));
        BucketConfiguration configOfBucket = BucketConfiguration.builder().addLimit(limit).build();
        Bucket bucket = proxyConfiguration.builder().build("a", configOfBucket);

        RateLimitAppConfig rateLimitAppConfig = new RateLimitAppConfig();
        rateLimitAppConfig.setId("id1");
        rateLimitAppConfig.setRateLimitEntity(new RateLimitEntity(1, null, 1, "j", bucket));
        rateLimitAppRepo.save(rateLimitAppConfig);

        bucket.tryConsume(1);

        Optional<BucketConfiguration> bucket2 = proxyManager.getProxyConfiguration("a");
        rateLimitAppConfig.setRateLimitEntity(new RateLimitEntity(1, null, 1, "j", bucket));
        rateLimitAppRepo.save(rateLimitAppConfig);
        //test



//         // checks in redis if config is present
//        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
//        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
//        RateLimitEntity rateLimitAppEntity;
//        RateLimitEntity rateLimitServiceEntity;
//
//
//        if(rateLimitAppConfig.isPresent())
//        {
//            rateLimitAppEntity = rateLimitAppConfig.get().getRateLimitEntity();
//            String b1 = getOrCreateBucket(appId, rateLimitAppEntity).getBucket();
//            bucketForApp = objectMapper.convertValue(b1,MyBucket.class);
//
//            if (!consumeTokensForApp(bucketForApp, rateLimitAppConfig.get())) {
//                return false;
//            }
//
//        }else{
//            return false;}
//
//        if(rateLimitServiceConfig.isPresent()){
//            rateLimitServiceEntity = rateLimitServiceConfig.get().getRateLimitEntity();
//            String b2 = getOrCreateBucket(appId + "+" + serviceName, rateLimitServiceEntity).getBucket();
//            bucketForService = objectMapper.convertValue(b2,MyBucket.class);
//
//            if (!consumeTokensForService(bucketForService,rateLimitServiceConfig.get())) {
//                return false;
//            }
//
//        }else{
//            return false;}
        // Both app and service checks passed
        return true;
    }



//    RateLimitEntity getOrCreateBucket(String id, RateLimitEntity rateLimitEntity) throws JsonProcessingException {
//        if(rateLimitEntity.getBucket()!=null)
//            return rateLimitEntity;
//        else{
//            return createAndSetBucket(id,rateLimitEntity);
//        }
//    }

//    boolean consumeTokensForApp(Bucket bucket,RateLimitAppConfig rateLimitAppConfig) throws JsonProcessingException {
//        if (bucket.tryConsume(1)) {
//            rateLimitAppConfig.getRateLimitEntity().setBucket(objectMapper.writeValueAsString(bucket));
//            rateLimitAppRepo.save(rateLimitAppConfig);
//            log.info("Tokens left are "+bucket.getAvailableTokens());
//            return true;
//        } else {
//            log.info("No tokens left");
//            return false;
//        }
//    }
//
//    boolean consumeTokensForService(Bucket bucket,RateLimitServiceConfig rateLimitServiceConfig) throws JsonProcessingException {
//        if (bucket.tryConsume(1)) {
//            rateLimitServiceConfig.getRateLimitEntity().setBucket(objectMapper.writeValueAsString(bucket));
//            rateLimitServiceRepo.save(rateLimitServiceConfig);
//            log.info("Tokens left are "+bucket.getAvailableTokens());
//            log.info(bucket.toString());
//            return true;
//        } else {
//            log.info("No tokens left");
//            return false;
//        }
//    }
//
//
//    RateLimitEntity createAndSetBucket(String id, RateLimitEntity rateLimitEntity) throws JsonProcessingException {
//        MyBucket newbucket = (MyBucket) createBucket(rateLimitEntity);
//        rateLimitEntity.setBucket(objectMapper.writeValueAsString(newbucket));
//        return rateLimitEntity;
//    }
//
//
//    Bucket createBucket(RateLimitEntity rateLimitEntity) {
//        long capacity = rateLimitEntity.getNoOfAllowedHits();
//        long duration = rateLimitEntity.getTime();
//        TimeUnit timeUnit = rateLimitEntity.getUnit();
//        return Bucket.builder().addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration))))).build();
//    }


//    private Supplier<BucketConfiguration> getConfigSupplierForUser(String userId) {
//        User user = userService.getUser(userId);
//
//        Refill refill = Refill.intervally(user.getLimit(), Duration.ofMinutes(1));
//        Bandwidth limit = Bandwidth.classic(user.getLimit(), refill);
//        return () -> (BucketConfiguration.builder().addLimit(limit).build());
//
//    }

}
