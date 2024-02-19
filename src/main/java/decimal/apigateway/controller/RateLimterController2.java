package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rate-limit2")
@Log
public class RateLimterController2 {
    @Autowired
    RateLimitRepo rateLimitRepo;

    @Autowired
    ObjectMapper objectMapper;


    @PostMapping
    private String createConfig(@RequestBody RateLimitConfigDto2 rateLimitConfigDto2) {
        if (rateLimitConfigDto2!=null){
            String appId = rateLimitConfigDto2.getAppId();
            String serviceName = rateLimitConfigDto2.getServiceName();

            RateLimitConfig rateLimitAppConfig = new RateLimitConfig(appId,null,rateLimitConfigDto2.getAppBucketConfig().getTime(),rateLimitConfigDto2.getAppBucketConfig().getUnit(),rateLimitConfigDto2.getAppBucketConfig().getNoOfAllowedHits(),rateLimitConfigDto2.getAppBucketConfig().getRateLimitLevel());
            rateLimitRepo.save(rateLimitAppConfig);

            RateLimitConfig rateLimitServiceConfig = new RateLimitConfig(appId+"+"+serviceName,null,rateLimitConfigDto2.getServiceBucketConfig().getTime(),rateLimitConfigDto2.getServiceBucketConfig().getUnit(),rateLimitConfigDto2.getServiceBucketConfig().getNoOfAllowedHits(),rateLimitConfigDto2.getServiceBucketConfig().getRateLimitLevel());
            rateLimitRepo.save(rateLimitServiceConfig);
            return "config created";
        }else{
            log.info("-------- config not created --------");
            return "config not created";
        }
    }

}
