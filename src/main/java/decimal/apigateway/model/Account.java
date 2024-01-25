package decimal.apigateway.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {


    private String username;
    private String password;
    private boolean enabled;

    // Http request data set after parsing of request
    private String requestId;
    private String authorization;
    private String nounce;

    private Map<String, String> accountData;

    // Parameters set after authentication and token creation
    private String authResponse;
    private String rsaKeys;
    private String jwtToken;
    private String appJwt;
    private String securityVersion;

    private ApplicationDef applicationDef;

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", requestId='" + requestId + '\'' +
                ", authorization='" + authorization + '\'' +
                ", nounce='" + nounce + '\'' +
                ", accountData=" + accountData +
                ", authResponse='" + authResponse + '\'' +
                ", rsaKeys='" + rsaKeys + '\'' +
                ", jwtToken='" + jwtToken + '\'' +
                ", appJwt='" + appJwt + '\'' +
                ", securityVersion='" + securityVersion + '\'' +
                ", applicationDef=" + applicationDef +
                '}';
    }
}
