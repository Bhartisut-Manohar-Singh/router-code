package decimal.apigateway.domain;


import decimal.apigateway.model.SSOTokenModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("SSO_TOKEN_DETAILS")
public class SSOTokenRedis implements Serializable {

    @Id
    private String id;

    @Indexed
    @NotBlank(message = "orgId is a mandatory field")
    public String orgId;

    @Indexed
    @NotBlank(message = "appId is mandatory field")
    public String appId;

    @Indexed
    @NotBlank(message = "loginId is a mandatory field")
    public String loginId;

    @Indexed
    public String ssoToken;

    public String expiryTime;

    public String data;

    public SSOTokenRedis(SSOTokenModel ssoTokenModel) {
        this.orgId=ssoTokenModel.getOrgId();
        this.appId=ssoTokenModel.getAppId();
        this.loginId=ssoTokenModel.getLoginId();
        this.expiryTime="5";
        this.ssoToken= UUID.randomUUID().toString();
        this.data= ssoTokenModel.getData();
    }
}
