package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateLimitAppRepo extends CrudRepository<RateLimitAppConfig, String> {

}
