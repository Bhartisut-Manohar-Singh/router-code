package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitIpConfig;
import org.springframework.data.repository.CrudRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RateLimitByIpRepository extends CrudRepository<RateLimitIpConfig, String> {

}
