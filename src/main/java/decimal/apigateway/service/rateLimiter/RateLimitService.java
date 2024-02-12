package decimal.apigateway.service.rateLimiter;

import decimal.apigateway.entity.*;
import decimal.apigateway.entity.BucketState;
import decimal.apigateway.repository.RateLimitAppRepo;
import decimal.apigateway.repository.RateLimitServiceRepo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@Log
public class RateLimitService {
    @Autowired
    RateLimitServiceRepo rateLimitServiceRepo;

    @Autowired
    RateLimitAppRepo rateLimitAppRepo;



    public boolean allowRequest(String appId, String serviceName) {

        BucketState bucketStateForApp;
        BucketState bucketStateForService;

        // checks in redis if config is present
        Optional<RateLimitAppConfig> rateLimitAppConfig = rateLimitAppRepo.findById(appId);
        Optional<RateLimitServiceConfig> rateLimitServiceConfig = rateLimitServiceRepo.findById(appId + "+" + serviceName);
        BucketConfig bucketConfigForApp;
        BucketConfig customBucketForService;


        if (rateLimitAppConfig.isPresent() && rateLimitAppConfig.get().getBucketConfig() != null) {
                bucketConfigForApp = rateLimitAppConfig.get().getBucketConfig();
                bucketConfigForApp = getOrCreateBucket(appId, bucketConfigForApp);

                if (!consumeTokensForApp(bucketConfigForApp, rateLimitAppConfig.get())) {
                    return false;
                }

            }else{
                return false;
            }

//        if(rateLimitServiceConfig.isPresent()){
//            rateLimitServiceEntity = rateLimitServiceConfig.get().getRateLimitEntity();
//            String b2 = getOrCreateBucket(appId + "+" + serviceName, rateLimitServiceEntity).getBucket();
//            bucketForService = objectMapper.convertValue(b2,Bucket.class);
//
//            if (!consumeTokensForService(bucketForService,rateLimitServiceConfig.get())) {
//                return false;
//            }
//
//        }else{
//            return false;}
            // Both app and service checks passed
            return true;
        }
    }



        BucketConfig getOrCreateBucket(String id, BucketConfig bucketConfig){
        //if bucket is present, return bucket or else, create bucket
        if(bucketConfig.getBucketState()!=null)
            return bucketConfig;
        else{
            return createAndSetBucket(id,bucketConfig);
        }
    }



    boolean consumeTokensForApp(BucketConfig bucketConfig,RateLimitAppConfig rateLimitAppConfig){
        // if tokens are there, consume, also check refill
        //check refill first(update refilll time and tokens) and update in repo
        long availableTokens = bucketConfig.getBucketState().getAvailableTokens();
        if (availableTokens > 0) {
            bucketConfig.getBucketState().setAvailableTokens(--availableTokens);
            rateLimitAppConfig.setBucketConfig(bucketConfig);
            rateLimitAppRepo.save(rateLimitAppConfig);
            log.info("Tokens left are "+ availableTokens);
            return true;
        } else {
            log.info("No tokens left");
            return false;
        }
    }

//    boolean consumeTokensForService(Bucket bucket,RateLimitServiceConfig rateLimitServiceConfig){
//        if (bucket.tryConsume(1)) {
//            rateLimitServiceConfig.getRateLimitEntity().setBucket(bucket.toString());
//            rateLimitServiceRepo.save(rateLimitServiceConfig);
//            log.info("Tokens left are "+bucket.getAvailableTokens());
//            log.info(bucket.toString());
//            return true;
//        } else {
//            log.info("No tokens left");
//            return false;
//        }
//    }


    BucketConfig createAndSetBucket(String id, BucketConfig bucketConfig){
        BucketState newState = createBucketState(bucketConfig);
        bucketConfig.setBucketState(newState);
        return bucketConfig;
    }


    BucketState createBucketState(BucketConfig bucketConfig) {
        BucketState createdState = new BucketState(bucketConfig.getNoOfAllowedHits(),null);
        return createdState;
    }


}

