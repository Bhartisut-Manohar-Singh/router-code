package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateLimitRepo extends CrudRepository<RateLimitConfig, String> {

}
