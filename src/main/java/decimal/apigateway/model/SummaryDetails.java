package decimal.apigateway.model;

import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.enums.Headers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDetails {

    private String orgId;

    private String appId;

    private String loginId;

    private String deviceId;

    public SummaryDetails(Map<String, String> httpHeaders) {


        String userName = httpHeaders.get(Headers.username.name());
        List<String> userNameData = AuthRouterOperations.getStringArray(userName, Constant.TILD_SPLITTER);

        this.orgId = userNameData.get(0);
        this.appId = userNameData.get(1);
        this.loginId = userNameData.get(2);

        String deviceDetails=httpHeaders.containsKey("username")?httpHeaders.get("username"):"";
        String[] split = deviceDetails.split("~");
        if(httpHeaders.containsKey("platform")){
            if ("WEB".equalsIgnoreCase(httpHeaders.get("platform"))){
                this.deviceId=split[2];
            }
            else{
                this.deviceId=split[3];

            }
        }


    }

}
