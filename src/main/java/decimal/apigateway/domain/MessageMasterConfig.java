package decimal.apigateway.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("MESSAGE_MASTER_CONFIG")
public class MessageMasterConfig {

    @Id
    private String id;

    @Indexed
    public String apiName;
    @Indexed
    public String orgId;
    @Indexed
    public String appId;
    @Indexed
    public String environment;


    @Indexed
    public String version;

    public String apiData;
}
