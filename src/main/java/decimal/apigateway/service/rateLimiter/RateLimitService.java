package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.exception.RequestNotPermitted;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.RateLimitRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
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
    private RedisTemplate<String, Long> redisTemplate;

    private ValueOperations<String, Long> valueOps;

    @PostConstruct
    private void init() {
        valueOps = redisTemplate.opsForValue();
    }


    public Boolean allowRequest(String appId, String serviceName, Map<String, String> httpHeaders) throws RouterException, IOException {

        Instant requestTimestamp = Instant.now();
        // checks in redis if rate limiting config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);

        if (rateLimitAppConfig.isPresent()) {
                if (!consumeTokens(rateLimitAppConfig.get(),RL_TILD+appId)) {
                 throw new RequestNotPermitted(NO_TOKENS_LEFT_FOR_APP+appId,requestTimestamp,httpHeaders);
                }

            }

        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + TILD_SPLITTER + serviceName);

        if(rateLimitServiceConfig.isPresent()){
            if (!consumeTokens(rateLimitServiceConfig.get(),RL_TILD+appId+TILD_SPLITTER+serviceName)) {
                throw new RequestNotPermitted(NO_TOKENS_LEFT_FOR_SERVICE+serviceName,requestTimestamp,httpHeaders);
            }
        }
            // Both app and service checks passed
            return true;
        }



    boolean consumeTokens(RateLimitConfig rateLimitConfig, String key){
        long convertedMilis = rateLimitConfig.getDurationUnit().toMillis(rateLimitConfig.getDuration());

        valueOps.setIfAbsent(key,rateLimitConfig.getMaxAllowedHits(),convertedMilis,TimeUnit.MILLISECONDS);

        if (redisTemplate.getExpire(key)==-1){
            log.info("+++++++++++++++ expiry was not set ++++++++++++++++++=");
            redisTemplate.expire(key,convertedMilis, TimeUnit.MILLISECONDS);
        }
        return valueOps.decrement(key) >= 0;
    }

}

