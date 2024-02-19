package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.entity.*;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.RateLimitRepo;
import decimal.apigateway.service.LogsWriter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
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

    @Autowired
    RedisTemplate redisTemplate;
    private ValueOperations valueOps;

    @PostConstruct
    private void init() {
        valueOps = redisTemplate.opsForValue();
    }


    public Boolean allowRequest(String appId, String serviceName, Map<String, String> httpHeaders) throws RouterException, IOException {
        auditPayload = logsWriter.initializeLog("request", JSON, httpHeaders);

        // checks in redis if config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);
        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "+" + serviceName);

        if (rateLimitAppConfig.isPresent()) {

                if (!consumeTokens(rateLimitAppConfig.get(),"rl~"+appId)) {
                    throw new RouterException(RouterResponseCode.TOO_MANY_REQUESTS_429, (Exception) null,FAILURE_STATUS, "No tokens left for this app. Please try again later.");
                }

            }else{
            throw new RouterException(INVALID_REQUEST_500, (Exception) null,FAILURE_STATUS, "No Configuration present in redis for this app.");
         }

        if (rateLimitServiceConfig.isEmpty()) {
            return true;
        }else{
            if (!consumeTokens(rateLimitServiceConfig.get(),"rl~"+appId+"+"+serviceName)) {
                throw new RouterException(RouterResponseCode.TOO_MANY_REQUESTS_429, (Exception) null,FAILURE_STATUS, "No tokens left for this service. Please try again later.");
            }

        }
            // Both app and service checks passed
            return true;
        }



        private void getOrCreateBucketState(RateLimitConfig rateLimitConfig, String key){
//            valueOps.set(key,"hh");
            log.info("-------created new config-------");
            valueOps.increment(key,Long.parseLong(rateLimitConfig.getNoOfAllowedHits()));
            String unitString = rateLimitConfig.getUnit();
            redisTemplate.expire(key, rateLimitConfig.getTime(), TimeUnit.valueOf(unitString.toUpperCase()));

    }



    boolean consumeTokens(RateLimitConfig rateLimitConfig, String key){
        if(!redisTemplate.hasKey(key)){
            getOrCreateBucketState(rateLimitConfig,key);
        }
        Long newCtr = valueOps.decrement(key);
        log.info("--------- tokens left are -------"+newCtr);
        if(newCtr<0){
            log.info("--- no tokens left ---");
            return false;
        }else {
            return true;
        }
    }

}

