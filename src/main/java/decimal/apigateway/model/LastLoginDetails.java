package decimal.apigateway.model;

import com.fasterxml.jackson.databind.JsonNode;
import decimal.apigateway.enums.Headers;
import decimal.apigateway.commons.ConstantsAuth;
import decimal.apigateway.commons.AuthRouterOperations;
import decimal.apigateway.enums.Headers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import static decimal.apigateway.service.util.BuiltInUtility.stringFormat;
import static decimal.apigateway.service.util.BuiltInUtility.stringFormat;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LastLoginDetails {

    private String orgId;

    private String appId;

    private String installationId;

    private String requestId;

    private String platform;

    private String simId;

    private String imeiNo;

    private String applicationVersion;

    private String osVersion;

    private String osDetails;

    private String sourceIp;

    private String userAgent;

    private String deviceTimestamp;

    private String deviceMake;

    private String deviceModel;

    private String installLatitute;

    private String installLongitude;

    private String loginId;

    private String deviceId;

    private String isNotificationOn;

    private String pushNotifDeviceId;


    public LastLoginDetails(JsonNode request, Map<String, String> httpHeaders) {
        JsonNode interfaces = request.get("interfaces");
        JsonNode services = request.get("services");

        String installationId = "";

            if ("WEB".equalsIgnoreCase(httpHeaders.get("platform")))
                installationId = httpHeaders.containsKey("deviceid") ? httpHeaders.get("deviceid") : "";
            else
                installationId = interfaces.has("IMEI_NO") ? interfaces.get("IMEI_NO").asText() : "";



        String osDetails = "";
        if (httpHeaders.containsKey("user-agent")) {
            String[] strings = httpHeaders.get("user-agent").split(" ");
                if ("WEB".equalsIgnoreCase(httpHeaders.get("platform"))) {
                    if (strings.length >= 4) {
                        String os = strings[1] + " " + strings[2] + " " + strings[3];
                        osDetails = os.substring(1, os.length() - 1).replace(";", "");
                    }
                } else
                    osDetails = stringFormat(httpHeaders.get("platform"))+" "+(interfaces.has("OS_VERSION") ? interfaces.get("OS_VERSION").asText() : "");

        }


        String userName = httpHeaders.get(Headers.username.name());
        List<String> userNameData = AuthRouterOperations.getStringArray(userName, ConstantsAuth.TILD_SPLITTER);

        this.orgId = userNameData.get(0);
        this.appId = userNameData.get(1);
        this.loginId = userNameData.get(2);
        this.installationId = installationId;
        this.requestId = httpHeaders.containsKey("requestid") ? httpHeaders.get("requestid") : "";
        this.platform = httpHeaders.containsKey("platform") ? httpHeaders.get("platform") : "";
        this.simId = "";
        this.imeiNo = interfaces.has("IMEI_NO") ? interfaces.get("IMEI_NO").asText() : "";
        this.applicationVersion = interfaces.has("APPLICATION_VERSION") ? interfaces.get("APPLICATION_VERSION").asText() : "";
        this.osVersion = interfaces.has("OS_VERSION") ? interfaces.get("OS_VERSION").asText() : "";
        this.osDetails = osDetails;
        this.sourceIp = httpHeaders.containsKey("x-forwarded-for")?httpHeaders.get("x-forwarded-for"):"";
        this.userAgent = httpHeaders.containsKey("user-agent") ? httpHeaders.get("user-agent") : "";
        this.deviceTimestamp = interfaces.has("DEVICE_TIMESTAMP") ? interfaces.get("DEVICE_TIMESTAMP").asText() : "";
        this.deviceModel = interfaces.has("DEVICE_MODEL") ? interfaces.get("DEVICE_MODEL").asText() : "";
        this.deviceMake = interfaces.has("DEVICE_MAKE") ? interfaces.get("DEVICE_MAKE").asText() : "";
        this.installLatitute = interfaces.has("DEVICE_LATITUDE") ? interfaces.get("DEVICE_LATITUDE").asText() : "";
        this.installLongitude = interfaces.has("DEVICE_LONGITUDE") ? interfaces.get("DEVICE_LONGITUDE").asText() : "";
        this.deviceId =installationId;
        this.isNotificationOn = "";
        this.pushNotifDeviceId=interfaces.has("PUSH_NOTIF_DEVICE_ID") ? interfaces.get("PUSH_NOTIF_DEVICE_ID").asText() :null;
    }
}
