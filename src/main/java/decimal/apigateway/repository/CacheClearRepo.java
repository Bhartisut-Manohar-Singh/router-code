package decimal.apigateway.repository;


import decimal.apigateway.domain.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheClearRepo extends CrudRepository<Session, String> {

    List<Session> findByOrgIdAndAppId(String orgId, String appId);
    List<Session> findTop1000ByOrgIdAndAppIdOrderByLastLoginAsc(String orgid, String appid);

}
