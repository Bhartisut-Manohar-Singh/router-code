package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SSOTokenResponse {

    private String status;
    private String message;
    private String errorDetails;
    private String ssoToken;
    private String data;

    public SSOTokenResponse(String status, String message, String errorDetails, String ssoToken) {
        this.status = status;
        this.message = message;
        this.errorDetails = errorDetails;
        this.ssoToken = ssoToken;
    }
}
