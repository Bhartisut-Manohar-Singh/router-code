package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInstallationDetails {

  private String username;
  private String requestId;
  private String orgId;
  private String appId;
  private String platform;
  private String platformDetails;
  private String deviceUdid;
  private String simId;
  private String deviceMake;
  private String deviceModel;
  private String installLatitude;
  private String installLongitude;
  private String ipAddress;
  private String osVersion;
  private String appVersionNo;
  private Timestamp appInstallTimestamp;
}
