package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.helper.Helper;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;


    Helper helper = new Helper();



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
        long lastRefill = bucketConfig.getBucketState().getLastRefillTime();
        LocalDateTime convertedLastRefill = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastRefill),
                ZoneId.systemDefault()
        );

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = convertedLastRefill.plus(duration);

        if(currentTime.isAfter(endTime)) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getNoOfAllowedHits());
            bucketConfig.getBucketState().setLastRefillTime(System.currentTimeMillis());
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("1 token consumed. Tokens left for app are --------- "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left for app ");
            return false;
        }
    }

    boolean consumeTokensForService(BucketConfig bucketConfig,RateLimitServiceConfig rateLimitServiceConfig){
        Duration duration = helper.findDuration(bucketConfig.getTime(),bucketConfig.getUnit());
        long lastRefill = bucketConfig.getBucketState().getLastRefillTime();
        LocalDateTime convertedLastRefill = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastRefill),
                ZoneId.systemDefault()
        );

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = convertedLastRefill.plus(duration);
        if(currentTime.isAfter(endTime)) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getNoOfAllowedHits());
            bucketConfig.getBucketState().setLastRefillTime(System.currentTimeMillis());
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            log.info("1 token consumed. Tokens left for service are --------------------- "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left for service");
            return false;
        }
    }


    BucketConfig createAndSetBucket(BucketConfig bucketConfig){
        BucketState newState = createBucketState(bucketConfig);
        bucketConfig.setBucketState(newState);
        return bucketConfig;
    }


    BucketState createBucketState(BucketConfig bucketConfig) {
        BucketState createdState = new BucketState(bucketConfig.getNoOfAllowedHits(),System.currentTimeMillis());
        return createdState;
    }


}

