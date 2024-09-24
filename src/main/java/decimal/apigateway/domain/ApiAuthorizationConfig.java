package decimal.apigateway.domain;

import decimal.apigateway.model.ApiListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("API_AUTHORIZATION_CONFIG")
public class ApiAuthorizationConfig implements Serializable {

    @Id
    private String id;
    @Indexed
    public String sourceAppId;
    @Indexed
    public String sourceOrgId;
    @Indexed
    public String destinationAppId;
    @Indexed
    public String destinationOrgId;
    @Indexed
    public String status;
    Set<ApiListResponse> accessApiListSet;

    @Override
    public String toString() {
        return "ApiAuthorizationConfig{" + "id='" + id + '\'' + ", sourceAppId='" + sourceAppId + '\'' + ", sourceOrgId='" + sourceOrgId + '\'' +
                ", destinationAppId='" + destinationAppId + '\'' + ", destinationOrgId='" + destinationOrgId + '\'' + ", status='" + status + '\'' + ", accessApiListSet=" + accessApiListSet + '}';
    }
    
}
