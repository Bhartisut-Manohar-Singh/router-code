package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.exception.RateLimitException;
import decimal.apigateway.helper.Helper;
import decimal.apigateway.repository.RateLimitRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    RateLimitRepo rateLimitRepo;
    Helper helper = new Helper();



    public Boolean allowRequest(String appId, String serviceName) {

        // checks in redis if config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);
        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "+" + serviceName);


        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig()!= null) {
            //check if ratelimitconfig is updated
                getOrCreateBucketState(rateLimitAppConfig.get());

                if (!consumeTokens(rateLimitAppConfig.get())) {
                    throw new RateLimitException("No tokens left for the app. Please try again later.",HttpStatus.TOO_MANY_REQUESTS);
                }

            }else{
                throw new RateLimitException(" No Configuration present in redis for this app. ", HttpStatus.NOT_FOUND);
            }

        if(rateLimitServiceConfig.isPresent() && rateLimitServiceConfig.get().getBucketConfig() != null){
            getOrCreateBucketState(rateLimitServiceConfig.get());

            if (!consumeTokens(rateLimitServiceConfig.get())) {
                throw new RateLimitException("No tokens left for the service. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
            }

        }else{
            throw new RateLimitException(" No Configuration present in redis for this service. ", HttpStatus.NOT_FOUND);}
            // Both app and service checks passed
            return true;
        }



        RateLimitConfig getOrCreateBucketState(RateLimitConfig rateLimitConfig){
        //if bucket state is present, return bucket or else, create bucket state
        if(rateLimitConfig.getBucketState()==null){
            BucketState newState = new BucketState(rateLimitConfig.getBucketConfig().getNoOfAllowedHits(),System.currentTimeMillis());
            rateLimitConfig.setBucketState(newState);
        }
        return rateLimitConfig;
    }



    boolean consumeTokens(RateLimitConfig rateLimitConfig){
        Duration duration = helper.findDuration(rateLimitConfig.getBucketConfig().getTime(),rateLimitConfig.getBucketConfig().getUnit());
        long lastRefill = rateLimitConfig.getBucketState().getLastRefillTime();
        LocalDateTime convertedLastRefill = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastRefill),
                ZoneId.systemDefault());

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endTime = convertedLastRefill.plus(duration);

        if(currentTime.isAfter(endTime)) {
            //refill and update last refill time
            rateLimitConfig.getBucketState().setAvailableTokens(bucketConfig.getNoOfAllowedHits());
            bucketConfig.getBucketState().setLastRefillTime(System.currentTimeMillis());
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
        }
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("1 token consumed.");
            return true;
        } else {
            log.info("No tokens left ");
            return false;
        }
    }

}

