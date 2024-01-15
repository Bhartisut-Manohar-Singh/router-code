package decimal.apigateway.service.redis;

import decimal.apigateway.domain.PublicAuthSession;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PublicAuthenticationSessionRepoRedis extends CrudRepository<PublicAuthSession, String> {
    List<PublicAuthSession> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId);

    List<PublicAuthSession> findByOrgIdAndAppId(String orgId, String appId);
}
