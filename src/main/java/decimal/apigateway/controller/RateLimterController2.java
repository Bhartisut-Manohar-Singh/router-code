package decimal.apigateway.controller;

import decimal.apigateway.entity.*;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitByIpRepository;
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
    RateLimitByIpRepository rateLimitByIpRepository;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;

    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;


    @PostMapping
    private String createConfig(@RequestBody RateLimitConfigDto2 rateLimitConfigDto2) {
        String id = rateLimitConfigDto2.getAppId();
        RateLimitAppConfig rateLimitAppConfig = new RateLimitAppConfig(id, rateLimitConfigDto2.getOrgId(), rateLimitConfigDto2.getAppId(), rateLimitConfigDto2.getAppRateLimitConfig());
        rateLimitAppRepo.save(rateLimitAppConfig);

        id = rateLimitConfigDto2.getAppId() + "+" + rateLimitConfigDto2.getServiceName();
        RateLimitServiceConfig rateLimitServiceConfig = new RateLimitServiceConfig(id, rateLimitConfigDto2.getOrgId(), rateLimitConfigDto2.getAppId(), rateLimitConfigDto2.getServiceName(), rateLimitConfigDto2.getServiceRateLimitConfig());

        rateLimitServiceRepo.save(rateLimitServiceConfig);
        return "config created";
//        if(rateLimitConfigDto2.getSourceIpRateLimitConfig()!=null){
//            RateLimitIpConfig rateLimitIpConfig = new RateLimitIpConfig();
//            rateLimitIpConfig.setSourceIp(rateLimitConfigDto2.getSourceIp());
//            rateLimitIpConfig.setRateLimitEntity(rateLimitConfigDto2.getSourceIpRateLimitConfig());
//            rateLimitByIpRepository.save(rateLimitIpConfig);
//            log.info("---------- config created for ip ----------");
//            return "config created for ip";
//
//        } else{
//            log.info("-------- config not created as ip not found --------");
//            return "config not created as ip not found";
//        }
//    }
    }
}
