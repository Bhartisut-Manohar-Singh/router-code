package decimal.apigateway.controller;

import decimal.apigateway.entity.RateLimitConfigDto2;
import decimal.apigateway.entity.RateLimitIpConfig;
import decimal.apigateway.repository.RateLimitByIpRepository;
import decimal.ratelimiter.config.RateLimitManager;
import decimal.ratelimiter.domain.RateLimiterConfigMaster;
import decimal.ratelimiter.repo.RateLimiterConfigRepo;
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
    RateLimitByIpRepository rateLimitByIpRepository;

//    @Autowired
//    RateLimitIpConfig rateLimitIpConfig;

    @PostMapping
    private void createConfig(@RequestBody RateLimitConfigDto2 rateLimitConfigDto2){
        RateLimitIpConfig rateLimitIpConfig = new RateLimitIpConfig();
        if(rateLimitConfigDto2.getSourceIpRateLimitConfig()!=null){
            rateLimitIpConfig.setSourceIp(rateLimitConfigDto2.getSourceIp());
            rateLimitIpConfig.setRateLimitEntity(rateLimitConfigDto2.getSourceIpRateLimitConfig());
            rateLimitByIpRepository.save(rateLimitIpConfig);
            log.info("---------- config created for ip ----------");
        } else{
            log.info("-------- config not created --------");
        }


    }
}
