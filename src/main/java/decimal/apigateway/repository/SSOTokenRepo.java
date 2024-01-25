package decimal.apigateway.repository;

import decimal.apigateway.domain.SSOTokenRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SSOTokenRepo extends CrudRepository<SSOTokenRedis,String> {

    Optional<SSOTokenRedis> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId);

    Optional<SSOTokenRedis> findByOrgIdAndAppIdAndLoginIdAndSsoToken(String orgId,String appId,String loginId,String ssoToken);





}
