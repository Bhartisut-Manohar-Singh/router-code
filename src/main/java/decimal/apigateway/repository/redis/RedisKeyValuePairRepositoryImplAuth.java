package decimal.apigateway.repository.redis;

import decimal.apigateway.domain.Session;
import decimal.apigateway.domain.TxnKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
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

    @Override
    public void expireSession(Session session, int expiryTimeInMinutes) {
        String session_expire ="AUTHENTICATION_SESSION:orgId:"+ session.getOrgId();
        stringRedisTemplate.expire(session_expire, expiryTimeInMinutes, TimeUnit.MINUTES);
        String session_expire1 ="AUTHENTICATION_SESSION:appId:" + session.getAppId();
        stringRedisTemplate.expire(session_expire1, expiryTimeInMinutes, TimeUnit.MINUTES);
        String session_expire2 ="AUTHENTICATION_SESSION:loginId:" + session.getLoginId();
        stringRedisTemplate.expire(session_expire2, expiryTimeInMinutes, TimeUnit.MINUTES);
        String session_expire3 ="AUTHENTICATION_SESSION:deviceId:" + session.getDeviceId();
        stringRedisTemplate.expire(session_expire3, expiryTimeInMinutes, TimeUnit.MINUTES);
        String session_expire4 ="AUTHENTICATION_SESSION:requestId:" + session.getRequestId();
        stringRedisTemplate.expire(session_expire4, expiryTimeInMinutes, TimeUnit.MINUTES);
        String  session_expire5 = "AUTHENTICATION_SESSION:" + session.getUsername();
        stringRedisTemplate.expire(session_expire5, expiryTimeInMinutes, TimeUnit.MINUTES);
        session_expire5 = session_expire5 + ":idx";
        stringRedisTemplate.expire(session_expire5, expiryTimeInMinutes, TimeUnit.MINUTES);

    }

    @Override
    public void expireTxnKey(TxnKey txnKey, int expiryTimeInMinutes) {

        String  txnKey_expire1 = "TXN_KEY:" + txnKey.getId();
        stringRedisTemplate.expire(txnKey_expire1, expiryTimeInMinutes, TimeUnit.MINUTES);
    }


}
