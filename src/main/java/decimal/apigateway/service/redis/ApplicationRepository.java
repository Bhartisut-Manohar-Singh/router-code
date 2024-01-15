package decimal.apigateway.service.redis;



import decimal.apigateway.domain.Session;

import java.util.Map;

public interface ApplicationRepository {
  
  Map<String, Session> findAllAppSession();
  void add(Session appSession);
  void delete(String id);
  Session findAppSession(String id);


}
