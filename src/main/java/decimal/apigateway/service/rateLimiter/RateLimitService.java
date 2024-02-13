package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.helper.Helper;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    Helper helper;



    public boolean allowRequest(String appId, String serviceName) {

        // checks in redis if config is present
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById("rl~" + appId);
        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById("rl~" + appId + "~" + serviceName);
        BucketConfig bucketConfigForApp;
        BucketConfig bucketConfigForService;


        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig() != null) {
                bucketConfigForApp = rateLimitAppConfig.get().getBucketConfig();
                bucketConfigForApp = getOrCreateBucket("rl~" + appId, bucketConfigForApp);

                if (!consumeTokensForApp(bucketConfigForApp, rateLimitAppConfig.get())) {
                    return false;
                }

            }else{
                return false;
            }

        if(rateLimitServiceConfig.isPresent() && rateLimitServiceConfig.get().getBucketConfig() != null){
            bucketConfigForService = rateLimitServiceConfig.get().getBucketConfig();
            bucketConfigForService = getOrCreateBucket("rl~" + appId + "~" + serviceName, bucketConfigForService);

            if (!consumeTokensForService(bucketConfigForService,rateLimitServiceConfig.get())) {
                return false;
            }

        }else{
            return false;}
            // Both app and service checks passed
            return true;
        }





        BucketConfig getOrCreateBucket(String id, BucketConfig bucketConfig){
        //if bucket is present, return bucket or else, create bucket
        if(bucketConfig.getBucketState()!=null)
            return bucketConfig;
        else{
            return createAndSetBucket(bucketConfig);
        }
    }



    boolean consumeTokensForApp(BucketConfig bucketConfig,RateLimitAppConfig rateLimitAppConfig){
        Duration duration = helper.findDuration(bucketConfig.getTime(),bucketConfig.getUnit());
        LocalDateTime lastRefill = bucketConfig.getBucketState().getLastRefillTime();

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = lastRefill.plus(duration);

        if(currentTime.isAfter(endTime)) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getNoOfAllowedHits());
            bucketConfig.getBucketState().setLastRefillTime(LocalDateTime.now());
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("Tokens left for app are --------- "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }

    boolean consumeTokensForService(BucketConfig bucketConfig,RateLimitServiceConfig rateLimitServiceConfig){
        Duration duration = helper.findDuration(bucketConfig.getTime(),bucketConfig.getUnit());
        LocalDateTime lastRefill = bucketConfig.getBucketState().getLastRefillTime();

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = lastRefill.plus(duration);
        if(currentTime.isAfter(endTime)) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getTime());
            bucketConfig.getBucketState().setLastRefillTime(LocalDateTime.now());
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            log.info("Tokens left for service are --------------------- "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }


    BucketConfig createAndSetBucket(BucketConfig bucketConfig){
        BucketState newState = createBucketState(bucketConfig);
        bucketConfig.setBucketState(newState);
        return bucketConfig;
    }


    BucketState createBucketState(BucketConfig bucketConfig) {
        BucketState createdState = new BucketState(bucketConfig.getNoOfAllowedHits(),LocalDateTime.now());
        return createdState;
    }


}

