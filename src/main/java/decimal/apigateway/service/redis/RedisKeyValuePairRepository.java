package decimal.apigateway.service.redis;

public interface RedisKeyValuePairRepository {

  void add(String key, String value, int expiryTimeInMinutes);
  void delete(String key);
  String get(String key);
}
