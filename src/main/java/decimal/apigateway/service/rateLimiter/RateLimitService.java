package decimal.apigateway.service.rateLimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.RateLimitRepo;
import decimal.apigateway.service.LogsWriter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitRepo rateLimitRepo;
    @Autowired
    AuditPayload auditPayload;
    @Autowired
    LogsWriter logsWriter;

    ObjectMapper objectMapper = new ObjectMapper();




    public Boolean allowRequest(String appId, String serviceName, Map<String, String> httpHeaders) throws RouterException, IOException {
        auditPayload = logsWriter.initializeLog("request", JSON, httpHeaders);

        // checks in redis if config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);
        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "+" + serviceName);

        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig()!= null) {
                getOrCreateBucketState(rateLimitAppConfig.get());

                if (!consumeTokens(rateLimitAppConfig.get())) {
                    throw new RouterException(RouterResponseCode.TOO_MANY_REQUESTS_429, (Exception) null,FAILURE_STATUS, "No tokens left for this app. Please try again later.");
                }

            }else{
            throw new RouterException(INVALID_REQUEST_500, (Exception) null,FAILURE_STATUS, "No Configuration present in redis for this app.");
         }

        if (rateLimitServiceConfig.isEmpty())
            return true;

        if(rateLimitServiceConfig.isPresent() && rateLimitServiceConfig.get().getBucketConfig() != null){
            getOrCreateBucketState(rateLimitServiceConfig.get());

            if (!consumeTokens(rateLimitServiceConfig.get())) {
                throw new RouterException(RouterResponseCode.TOO_MANY_REQUESTS_429, (Exception) null,FAILURE_STATUS, "No tokens left for this service. Please try again later.");
            }

        }else{
            throw new RouterException(INVALID_REQUEST_500, (Exception) null,FAILURE_STATUS, "No Configuration present in redis for this service.");
        }
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
        long nextRefill = rateLimitConfig.getBucketState().getNextRefillTime();
        long currentTime = System.currentTimeMillis();

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
            log.info("-------------1 token consumed. available tokens are ------------" + availableTokens);
            return true;
        } else {
            log.info("------------No tokens left -----------");
            return false;
        }

    }

    private long findNextRefill(RateLimitConfig rateLimitConfig) {

        long time = rateLimitConfig.getBucketConfig().getTime();
        String unitString = rateLimitConfig.getBucketConfig().getUnit();
        TimeUnit unit = TimeUnit.valueOf(unitString.toUpperCase());

        long currentTime = System.currentTimeMillis();
        return currentTime + unit.toMillis(time);
    }
}

