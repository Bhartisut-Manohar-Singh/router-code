package decimal.apigateway.repository;

import decimal.apigateway.entity.RateLimitAppAndServiceConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitAppAndServiceRepo extends CrudRepository<RateLimitAppAndServiceConfig, String> {
    Optional<RateLimitAppAndServiceConfig> findByIdAndOrgId(String id, String orgId);

}
