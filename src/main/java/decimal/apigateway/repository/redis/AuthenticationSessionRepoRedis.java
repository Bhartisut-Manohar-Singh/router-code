package decimal.apigateway.repository.redis;

import decimal.apigateway.domain.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthenticationSessionRepoRedis extends CrudRepository<Session, String> {
    List<Session> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId);

    List<Session> findByOrgIdAndAppIdAndDeviceId(String orgId, String appId, String deviceId);

    List<Session> findByOrgIdAndAppId(String orgId, String appId);
}
