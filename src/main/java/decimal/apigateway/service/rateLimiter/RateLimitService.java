package decimal.apigateway.service.rateLimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import io.github.bucket4j.*;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import io.github.bucket4j.local.LockFreeBucket;
import io.github.bucket4j.local.SynchronizedBucket;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
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
    ObjectMapper objectMapper;


    public boolean allowRequest(String appId, String serviceName) throws IOException {
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
            String b1 = getOrCreateBucket(appId, rateLimitAppEntity).getBucket();
            bucketForApp = Bucket4j.fromString(str);




            if (!consumeTokensForApp(bucketForApp, rateLimitAppConfig.get())) {
                return false;
            }

        }else{
            return false;}

        if(rateLimitServiceConfig.isPresent()){
            rateLimitServiceEntity = rateLimitServiceConfig.get().getRateLimitEntity();
            String b2 = getOrCreateBucket(appId + "+" + serviceName, rateLimitServiceEntity).getBucket();
            bucketForService = objectMapper.convertValue(b2,MyBucket.class);

            if (!consumeTokensForService(bucketForService,rateLimitServiceConfig.get())) {
                return false;
            }

        }else{
            return false;}
        // Both app and service checks passed
        return true;
    }



    RateLimitEntity getOrCreateBucket(String id, RateLimitEntity rateLimitEntity){
        if(rateLimitEntity.getBucket()!=null)
            return rateLimitEntity;
        else{
            return createAndSetBucket(id,rateLimitEntity);
        }
    }

    boolean consumeTokensForApp(Bucket bucket,RateLimitAppConfig rateLimitAppConfig){
        if (bucket.tryConsume(1)) {
            rateLimitAppConfig.getRateLimitEntity().setBucket(bucket.toString());
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("Tokens left are "+bucket.getAvailableTokens());
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }

    boolean consumeTokensForService(Bucket bucket,RateLimitServiceConfig rateLimitServiceConfig){
        if (bucket.tryConsume(1)) {
            rateLimitServiceConfig.getRateLimitEntity().setBucket(bucket.toString());
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            log.info("Tokens left are "+bucket.getAvailableTokens());
            log.info(bucket.toString());
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }


    RateLimitEntity createAndSetBucket(String id, RateLimitEntity rateLimitEntity){
        Bucket newbucket = createBucket(rateLimitEntity);
        rateLimitEntity.setBucket(newbucket.toString());
        return rateLimitEntity;
    }


    Bucket createBucket(RateLimitEntity rateLimitEntity) {
        long capacity = rateLimitEntity.getNoOfAllowedHits();
        long duration = rateLimitEntity.getTime();
        TimeUnit timeUnit = rateLimitEntity.getUnit();
        BucketConfiguration bucketConfiguration = Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration)))))
                .buildConfiguration();
        Bucket bucket = new LockFreeBucket(bucketConfiguration,TimeMeter.SYSTEM_MILLISECONDS);

//        Bucket bucket = Bucket4j.builder().addConfiguration(bucketConfiguration).build();


            return  bucket;
//        return Bucket.builder().addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMillis(timeUnit.toMillis(duration))))).build();
    }


}
