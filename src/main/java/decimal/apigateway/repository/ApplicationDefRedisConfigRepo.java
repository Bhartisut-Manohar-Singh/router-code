package decimal.apigateway.repository;



import decimal.apigateway.domain.ApplicationDefRedisConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApplicationDefRedisConfigRepo extends CrudRepository<ApplicationDefRedisConfig, String> {

    Optional<ApplicationDefRedisConfig> findByOrgIdAndAppId(String orgId, String appId);
}
