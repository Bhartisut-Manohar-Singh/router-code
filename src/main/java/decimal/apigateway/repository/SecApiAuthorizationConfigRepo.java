package decimal.apigateway.repository;

import decimal.apigateway.domain.ApiAuthorizationConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecApiAuthorizationConfigRepo extends CrudRepository<ApiAuthorizationConfig, String> {
    Optional<ApiAuthorizationConfig> findBySourceAppIdAndDestinationAppId(String  sourceAppId, String destinationAppId);
    Optional<ApiAuthorizationConfig> findBySourceOrgIdAndSourceAppId(String sourceOrgId, String sourceAppId);
    Optional<ApiAuthorizationConfig> findBySourceOrgIdAndSourceAppIdAndDestinationAppId(String sourceOrgId, String sourceAppId,String destinationAppId);

}
