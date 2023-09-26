package decimal.apigateway.controller;

import decimal.ratelimiter.config.RateLimitManager;
import decimal.ratelimiter.domain.RateLimiterConfigMaster;
import decimal.ratelimiter.repo.RateLimiterConfigRepo;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rate-limit")
@Log
public class RateLimiterConfigController {

    @Autowired
    private RateLimiterConfigRepo rmConfigRepo;

    @Autowired
    private RateLimitManager rateLimitManager;

    @PostMapping
    private void createConfig(@RequestBody RateLimiterConfigMaster master){
        rmConfigRepo.save(master);
    }

    @PutMapping
    public RateLimiter updateRateLimiter(@RequestParam String key, @RequestParam int value){
        log.info("updating rate limit for key - " + key + " with value - " + value);
        return rateLimitManager.updateRateLimit(key, value);
    }

    @DeleteMapping
    public RateLimiter deleteRateLimiter(@RequestParam String key){
        log.info("Deleting rate limit for key - " + key);
        return rateLimitManager.deleteRateLimiter(key);
    }
}
