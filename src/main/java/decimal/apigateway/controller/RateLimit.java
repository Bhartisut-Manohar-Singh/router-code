package decimal.apigateway.controller;

import decimal.apigateway.entity.RateLimitConfig;
import decimal.apigateway.entity.RateLimitConfigDto2;
import decimal.apigateway.repository.RateLimitRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("rate-limit2")
public class RateLimit {
    @Autowired
    RateLimitRepo rateLimitRepo;
    @PostMapping("")
    public void rateController(){
        RateLimitConfig rateLimitConfig = new RateLimitConfig();
        rateLimitConfig.setId("abc");
        rateLimitConfig.setApiName("api");
        rateLimitConfig.setMaxAllowedHits(5);
        rateLimitConfig.setDuration(10);
        rateLimitConfig.setDurationUnit(TimeUnit.SECONDS);
        rateLimitRepo.save(rateLimitConfig);
    }
}
