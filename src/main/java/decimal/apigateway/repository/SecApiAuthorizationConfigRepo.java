package decimal.apigateway.repository;

import decimal.apigateway.domain.ApiAuthorizationConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SecApiAuthorizationConfigRepo extends CrudRepository<ApiAuthorizationConfig, String> {
    Optional<ApiAuthorizationConfig> findBySourceAppIdAndDestinationAppId(String  sourceAppId, String destinationAppId);

}
