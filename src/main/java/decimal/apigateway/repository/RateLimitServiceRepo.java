package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppConfig;
import decimal.apigateway.entity.RateLimitServiceConfig;
import org.springframework.data.repository.CrudRepository;

public interface RateLimitServiceRepo extends CrudRepository<RateLimitServiceConfig, String> {
}
