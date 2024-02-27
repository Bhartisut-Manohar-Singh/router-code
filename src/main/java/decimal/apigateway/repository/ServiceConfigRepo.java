package decimal.apigateway.repository;

import decimal.apigateway.domain.ServiceConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceConfigRepo extends CrudRepository<ServiceConfig, String>
{
    List<ServiceConfig> findByOrgIdAndAppIdAndApiName(String orgId, String appId, String serviceName);
}
