package decimal.apigateway.repository.redis;

import decimal.apigateway.domain.PublicAuthSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicAuthenticationSessionRepoRedis extends CrudRepository<PublicAuthSession, String> {
    List<PublicAuthSession> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId);

    List<PublicAuthSession> findByOrgIdAndAppId(String orgId, String appId);
}
