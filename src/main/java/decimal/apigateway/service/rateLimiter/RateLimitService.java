package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.helper.Helper;
import decimal.apigateway.repository.RateLimitRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitRepo rateLimitRepo;
    Helper helper = new Helper();



    public Boolean allowRequest(String appId, String serviceName) throws RouterException {

        // checks in redis if config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);
        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "+" + serviceName);

        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig()!= null) {
                getOrCreateBucketState(rateLimitAppConfig.get());

                if (!consumeTokens(rateLimitAppConfig.get())) {
                    throw new RouterException(TOO_MANY_REQUESTS_429, "No tokens left for the app. Please try again later.", null);
                }

            }else{
                throw new RouterException(INVALID_REQUEST_500, " No Configuration present in redis for this app. ", null);
            }

        //check
        if (rateLimitServiceConfig.isEmpty())
            return true;

        if(rateLimitServiceConfig.isPresent() && rateLimitServiceConfig.get().getBucketConfig() != null){
            getOrCreateBucketState(rateLimitServiceConfig.get());

            if (!consumeTokens(rateLimitServiceConfig.get())) {
                throw new RouterException(TOO_MANY_REQUESTS_429, "No tokens left for the service. Please try again later.", null);
            }

        }else{
            throw new RouterException(INVALID_REQUEST_500, " No Configuration present in redis for this service. ", null);}
            // Both app and service checks passed
            return true;
        }



        RateLimitConfig getOrCreateBucketState(RateLimitConfig rateLimitConfig){
        //if bucket state is present, return bucket or else, create bucket state
        if(rateLimitConfig.getBucketState()==null){
            long nextRefillTime = findNextRefill(rateLimitConfig);
            BucketState newState = new BucketState(rateLimitConfig.getBucketConfig().getNoOfAllowedHits(),nextRefillTime);

            rateLimitConfig.setBucketState(newState);
        }
        return rateLimitConfig;
    }



    boolean consumeTokens(RateLimitConfig rateLimitConfig){
//        Duration duration = helper.findDuration(rateLimitConfig.getBucketConfig().getTime(),rateLimitConfig.getBucketConfig().getUnit());

        long nextRefill = rateLimitConfig.getBucketState().getNextRefillTime();
//        LocalDateTime convertedNextRefill = LocalDateTime.ofInstant(
//                Instant.ofEpochMilli(nextRefill),
//                ZoneId.systemDefault());
        long currentTime = System.currentTimeMillis();

//        LocalDateTime endTime = convertedLastRefill.plus(duration);

        if(currentTime>nextRefill) {
            //refill and update last refill time
            rateLimitConfig.getBucketState().setAvailableTokens(rateLimitConfig.getBucketConfig().getNoOfAllowedHits());
            nextRefill = findNextRefill(rateLimitConfig);
            rateLimitConfig.getBucketState().setNextRefillTime(nextRefill);
        }
        long availableTokens = rateLimitConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            rateLimitConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitRepo.save(rateLimitConfig);
            log.info("1 token consumed.");
            return true;
        } else {
            log.info("No tokens left ");
            return false;
        }

    }

    private long findNextRefill(RateLimitConfig rateLimitConfig) {
        long time = rateLimitConfig.getBucketConfig().getTime();
        String unitString = rateLimitConfig.getBucketConfig().getUnit();
        TimeUnit unit = TimeUnit.valueOf(unitString.toUpperCase());
        return System.currentTimeMillis() + unit.toMillis(time);
    }
}

