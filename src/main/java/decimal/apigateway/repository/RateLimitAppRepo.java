package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppConfig;
import decimal.apigateway.entity.RateLimitServiceConfig;
import io.github.bucket4j.BucketConfiguration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.function.Supplier;

@Repository
public interface RateLimitAppRepo extends CrudRepository<RateLimitAppConfig, String> {

}
