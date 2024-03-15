package decimal.apigateway.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("ORCA_CONFIG")
public class ServiceConfig implements Serializable {

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
