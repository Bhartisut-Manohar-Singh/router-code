package decimal.apigateway.service.rateLimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.entity.*;
import decimal.apigateway.exception.RequestNotPermitted;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.RateLimitRepo;
import decimal.apigateway.service.LogsWriter;
import decimal.logs.model.AuditPayload;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


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
    private RedisTemplate<String, Long> redisTemplate;

    private ValueOperations<String, Long> valueOps;

    @PostConstruct
    private void init() {
        valueOps = redisTemplate.opsForValue();
    }


    public Boolean allowRequest(String appId, String serviceName, Map<String, String> httpHeaders) throws RouterException, IOException {
        auditPayload = logsWriter.initializeLog("request", JSON, httpHeaders);

        // checks in redis if rate limiting config is present
        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);

        if (rateLimitAppConfig.isPresent()) {
            log.info("------- going to consume token for app ---------");
                if (!consumeTokens(rateLimitAppConfig.get(),"rl~"+appId)) {
                    throw new RequestNotPermitted("No tokens left for this app. Please try again later.");
                }

            }

        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "~" + serviceName);

        if(rateLimitServiceConfig.isPresent()){
            log.info("------- going to consume token for service ---------");
            if (!consumeTokens(rateLimitServiceConfig.get(),"rl~"+appId+"~"+serviceName)) {
                throw new RequestNotPermitted("No tokens left for this service. Please try again later.");
            }
        }
            // Both app and service checks passed
            return true;
        }



    boolean consumeTokens(RateLimitConfig rateLimitConfig, String key){
        log.info("------ inside consume tokens---------");
//        if(!redisTemplate.hasKey(key)){
//            valueOps.set(key,rateLimitConfig.getMaxAllowedHits(),rateLimitConfig.getDuration(),rateLimitConfig.getDurationUnit());
//            log.info("-------created new config-------"+rateLimitConfig.getMaxAllowedHits()+"-------------"+rateLimitConfig.getDuration()+"-------------"+rateLimitConfig.getDurationUnit());
//        }
        Boolean bool = valueOps.setIfAbsent(key,rateLimitConfig.getMaxAllowedHits(),rateLimitConfig.getDuration(),rateLimitConfig.getDurationUnit());
        log.info("-------- returned value after setting the key --------"+bool);

        Long newCtr = valueOps.decrement(key);
        log.info("--------- tokens left are ------- : "+newCtr);
        if(newCtr<0){
            log.info("--- no tokens left ---");
            return false;
        }else {
            return true;
        }


    }

}

