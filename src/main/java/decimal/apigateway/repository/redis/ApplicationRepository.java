package decimal.apigateway.repository.redis;



import decimal.apigateway.domain.Session;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface ApplicationRepository {
  
  Map<String, Session> findAllAppSession();
  void add(Session appSession);
  void delete(String id);
  Session findAppSession(String id);


}
