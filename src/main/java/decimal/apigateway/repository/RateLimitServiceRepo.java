package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppConfig;
import decimal.apigateway.entity.RateLimitServiceConfig;
import decimal.apigateway.service.rateLimiter.RateLimitService;
import org.springframework.data.repository.CrudRepository;

public interface RateLimitServiceRepo extends CrudRepository<RateLimitServiceConfig, String> {
}
