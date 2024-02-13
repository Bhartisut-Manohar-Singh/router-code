package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;



    public boolean allowRequest(String appId, String serviceName) {

        BucketState bucketStateForApp;
        BucketState bucketStateForService;

        // checks in redis if config is present
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
        BucketConfig bucketConfigForApp;
        BucketConfig bucketConfigForService;


        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig() != null) {
                bucketConfigForApp = rateLimitAppConfig.get().getBucketConfig();
                bucketConfigForApp = getOrCreateBucket(appId, bucketConfigForApp);

                if (!consumeTokensForApp(bucketConfigForApp, rateLimitAppConfig.get())) {
                    return false;
                }

            }else{
                return false;
            }

        if(rateLimitServiceConfig.isPresent() && rateLimitServiceConfig.get().getBucketConfig() != null){
            bucketConfigForService = rateLimitServiceConfig.get().getBucketConfig();
            bucketConfigForService = getOrCreateBucket(appId + "+" + serviceName, bucketConfigForService);

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
            return createAndSetBucket(id,bucketConfig);
        }
    }



    boolean consumeTokensForApp(BucketConfig bucketConfig,RateLimitAppConfig rateLimitAppConfig){
        TimeUnit unit = bucketConfig.getUnit();
        long timeInMilliseconds = unit.toMillis(bucketConfig.getTime());
        if(bucketConfig.getBucketState().getLastRefillTime().getTime()+timeInMilliseconds> System.currentTimeMillis()) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getTime());
            bucketConfig.getBucketState().setLastRefillTime(Timestamp.valueOf(LocalDateTime.now()));
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("Tokens left are "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }

    boolean consumeTokensForService(BucketConfig bucketConfig,RateLimitServiceConfig rateLimitServiceConfig){
        TimeUnit unit = bucketConfig.getUnit();
        long timeInMilliseconds = unit.toMillis(bucketConfig.getTime());
        if(bucketConfig.getBucketState().getLastRefillTime().getTime()+timeInMilliseconds> System.currentTimeMillis()) {
            //refill and update last refill time
            bucketConfig.getBucketState().setAvailableTokens(bucketConfig.getTime());
            bucketConfig.getBucketState().setLastRefillTime(Timestamp.valueOf(LocalDateTime.now()));
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitServiceConfig.setBucketConfig(bucketConfig);
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            log.info("Tokens left are "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }


    BucketConfig createAndSetBucket(String id, BucketConfig bucketConfig){
        BucketState newState = createBucketState(bucketConfig);
        bucketConfig.setBucketState(newState);
        return bucketConfig;
    }


    BucketState createBucketState(BucketConfig bucketConfig) {
        BucketState createdState = new BucketState(bucketConfig.getNoOfAllowedHits(),null);
        return createdState;
    }


}

