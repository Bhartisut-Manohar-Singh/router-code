package decimal.apigateway.repository;



import decimal.apigateway.domain.ApplicationDefRedisConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationDefRedisConfigRepo extends CrudRepository<ApplicationDefRedisConfig, String> {

    Optional<ApplicationDefRedisConfig> findByOrgIdAndAppId(String orgId, String appId);

    Optional<ApplicationDefRedisConfig> findByAppId(String appId);
}
