package decimal.apigateway.repository;

import decimal.apigateway.domain.MessageMasterConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MessageMasterConfigRepo extends CrudRepository<MessageMasterConfig,String> {

    List<MessageMasterConfig> findByApiName(String apiName);
    Optional<MessageMasterConfig> findByOrgIdAndAppIdAndApiName(String org, String app, String apiName);
}
