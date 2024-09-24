package decimal.apigateway.repository.redis;

import decimal.apigateway.domain.Session;
import decimal.apigateway.domain.TxnKey;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisKeyValuePairRepository {

  void add(String key, String value, int expiryTimeInMinutes);
  void delete(String key);
  String get(String key);

  void expireSession(Session session, int expiryTimeInHours);

  void expireTxnKey(TxnKey txnKey, int expiryTimeInMinutes);
}
