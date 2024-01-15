package decimal.apigateway.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("PUBLIC_AUTHENTICATION_SESSION")
public class PublicAuthSession {

    @Id
    private String username;

    @Indexed
    private String orgId;

    @Indexed
    private String appId;

    @Indexed
    private String loginId;

    private String sessionId;
    private String jwtKey;
    private String lastLogin;

    @Indexed
    private String requestId;
    private String securityVersion;
}
