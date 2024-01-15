package decimal.apigateway.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisKeyValuePairRepositoryImplAuth implements RedisKeyValuePairRepository {

    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOps;

    @PostConstruct
    private void init() {
        valueOps = stringRedisTemplate.opsForValue();
    }

    @Autowired
    public RedisKeyValuePairRepositoryImplAuth(StringRedisTemplate redisTemplate) {
        this.stringRedisTemplate = redisTemplate;
    }

    @Override
    public void add(String key, String value, int expiryTimeInMinutes) {
        valueOps.set(key, value);
        stringRedisTemplate.expire(key, expiryTimeInMinutes, TimeUnit.MINUTES);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public String get(String key) {
        return valueOps.get(key);
    }

}
