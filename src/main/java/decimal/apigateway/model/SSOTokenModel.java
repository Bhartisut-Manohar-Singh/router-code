package decimal.apigateway.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SSOTokenModel {

    private String orgId;

    private String appId;

    private String loginId;

    private String ssoToken;

    private String data;
}
