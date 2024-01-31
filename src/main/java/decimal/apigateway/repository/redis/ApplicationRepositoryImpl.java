package decimal.apigateway.repository.redis;

import decimal.apigateway.domain.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class ApplicationRepositoryImpl implements ApplicationRepository {

  private static final String KEY = "APP";
  private HashOperations<String, String, Session> hashOperations;

  @Autowired
  RedisTemplate<String, Object> redisTemplate;

  @PostConstruct
  private void init() {
    hashOperations = redisTemplate.opsForHash();
  }


  @Override
  public Map<String, Session> findAllAppSession() {
    return hashOperations.entries(KEY);
  }

  @Override
  public void add(Session appSession) {
    hashOperations.put(KEY, appSession.getUsername(), appSession);

  }

  @Override
  public void delete(String id) {
    hashOperations.delete(KEY, id);

  }

  @Override
  public Session findAppSession(String id) {
    return hashOperations.get(KEY, id);
  }
}
