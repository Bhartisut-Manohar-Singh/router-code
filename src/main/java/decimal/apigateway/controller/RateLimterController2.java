package decimal.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
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
    RateLimitAppRepo rateLimitAppRepo;

    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    ObjectMapper objectMapper;


    @PostMapping
    private String createConfig(@RequestBody RateLimitConfigDto2 rateLimitConfigDto2) {
        if (rateLimitConfigDto2!=null){
            String appId = rateLimitConfigDto2.getAppId();
            String serviceName = rateLimitConfigDto2.getServiceName();
            RateLimitAppConfig rateLimitAppConfig = new RateLimitAppConfig("rl~"+appId, rateLimitConfigDto2.getAppRateLimitConfig());
            rateLimitAppRepo.save(rateLimitAppConfig);

            RateLimitServiceConfig rateLimitServiceConfig = new RateLimitServiceConfig("rl~" + appId + "~" + serviceName, rateLimitConfigDto2.getServiceRateLimitConfig());
            rateLimitServiceRepo.save(rateLimitServiceConfig);
            return "config created";
        }else{
            log.info("-------- config not created --------");
            return "config not created";
        }
    }

}
