package decimal.apigateway.model;

import lombok.Data;


@Data
public class UserList {

    private String username;

    private String orgId;

    private String appId;

    private String loginId;

    private String deviceId;

    private String sessionId;

    private String lastLogin;

    public UserList(String username, String orgId, String appId, String loginId, String deviceId, String sessionId, String lastLogin) {
        this.username = username;
        this.orgId = orgId;
        this.appId = appId;
        this.loginId = loginId;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.lastLogin=lastLogin;
    }

    public UserList(){}
}
