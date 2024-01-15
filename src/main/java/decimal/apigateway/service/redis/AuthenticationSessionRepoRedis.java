package decimal.apigateway.service.redis;

import decimal.apigateway.domain.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AuthenticationSessionRepoRedis extends CrudRepository<Session, String> {
    List<Session> findByOrgIdAndAppIdAndLoginId(String orgId, String appId, String loginId);

    List<Session> findByOrgIdAndAppIdAndDeviceId(String orgId, String appId, String deviceId);

    List<Session> findByOrgIdAndAppId(String orgId, String appId);
}
