package decimal.apigateway.service.rateLimiter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.commons.RouterResponseCode;
import decimal.apigateway.entity.*;
import decimal.apigateway.exception.RateLimitError;
import decimal.apigateway.exception.RequestNotPermitted;
import decimal.apigateway.exception.RouterException;
import decimal.apigateway.repository.RateLimitRepo;
import decimal.apigateway.service.LogsWriter;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.Request;
import decimal.logs.model.Response;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;


import static decimal.apigateway.commons.Constant.*;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitRepo rateLimitRepo;
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    LogsWriter logsWriter;

    @Autowired
    AuditTraceFilter auditTraceFilter;

    private ValueOperations<String, Long> valueOps;

    @PostConstruct
    private void init() {
        valueOps = redisTemplate.opsForValue();
    }


    public Object allowRequest(String appId, String serviceName, Map<String, String> httpHeaders) throws IOException {

        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());

        auditPayload = logsWriter.initializeLog("request", JSON,httpHeaders,serviceName,auditPayload);
        auditTraceFilter.setIsServicesLogsEnabled(true);

        Optional<RateLimitConfig> rateLimitAppConfig = rateLimitRepo.findById(appId);

        if (rateLimitAppConfig.isPresent()) {
            log.info("------- going to consume token for app ---------");
                if (!consumeTokens(rateLimitAppConfig.get(),"RL~"+appId)) {
                 //throw new RequestNotPermitted("No tokens left for this app. Please try again later.");
                    return failureResponse(auditPayload);
                }

            }

        Optional<RateLimitConfig> rateLimitServiceConfig = rateLimitRepo.findById(appId + "~" + serviceName);

        if(rateLimitServiceConfig.isPresent()){
            log.info("------- going to consume token for service ---------");
            if (!consumeTokens(rateLimitServiceConfig.get(),"RL~"+appId+"~"+serviceName)) {
              //  throw new RequestNotPermitted("No tokens left for this service. Please try again later.");
                return failureResponse(auditPayload);

            }
        }
            return true;
        }

    private Object failureResponse(AuditPayload auditPayload) throws JsonProcessingException {
        RateLimitError rateLimitError = new RateLimitError("Too many requests",FAILURE_STATUS,HttpStatus.TOO_MANY_REQUESTS.value());

        auditPayload.getResponse().setResponse(mapper.writeValueAsString(rateLimitError));
        auditPayload.getResponse().setStatus(FAILURE_STATUS);
        auditPayload.getResponse().setTimestamp(Instant.now());
        auditPayload.setStatus(FAILURE_STATUS);
        logsWriter.updateLog(auditPayload);

        return new ResponseEntity<>(rateLimitError, null, HttpStatus.TOO_MANY_REQUESTS);
    }


    boolean consumeTokens(RateLimitConfig rateLimitConfig, String key){
        log.info("------ inside consume tokens---------");

        Boolean bool = valueOps.setIfAbsent(key,rateLimitConfig.getMaxAllowedHits(),rateLimitConfig.getDuration(),rateLimitConfig.getDurationUnit());
        log.info("-------- returned value after setting the key --------"+bool);

        if (redisTemplate.getExpire(key)==-1){
            log.info("+++++++++++++++ expiry was not set ++++++++++++++++++=");
            redisTemplate.expire(key,rateLimitConfig.getDuration(),rateLimitConfig.getDurationUnit());
        }

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

