package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppConfig;
import decimal.apigateway.entity.RateLimitServiceConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitAppRepo extends CrudRepository<RateLimitAppConfig, String> {

}
