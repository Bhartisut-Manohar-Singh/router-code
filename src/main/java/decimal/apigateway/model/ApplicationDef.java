package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDef implements Serializable {
    private static final long serialVersionUID = -2796610804379394876L;

    private String orgId;
    private String appId;
    private String clientSecret;
    private Long userSessionExpiryTime;
    private Long appSessionExpiryTime;
    private String isMultipleSessionAllowed;
    private String isUserInactiveSessionRequired;
    private Long userInactiveSessionExpiryTime;
    private String isSourceIPValidationRequired;
    private String allowedSourceIPs;
    private String secureMode;//SESSION or KEY
    private String isServerLogRequired;
    private String status;
    private String isSSOEnabled;
    private String allowedOrgApp;
}
