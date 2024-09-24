package decimal.apigateway.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import decimal.apigateway.commons.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDetails{

	private String orgId;
	private String appId;
	private String loginId;
	private String username;
	private String imeiNo;
	private String requestId;
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

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:MM:ss.SSS", timezone = "IST")
	private Date userLoginTimestamp;

	private String userLoginLatitude;
	private String userLoginLongitude;
	private String status;

	@JsonProperty("interfaces")
	public void getInterfaces(Map<String, String> interfaces)
	{
		this.userLoginLatitude = interfaces.get("DEVICE_LATITUDE");
		this.userLoginLongitude = interfaces.get("DEVICE_LONGITUDE");
		this.userLoginTimestamp = new Date();

		this.deviceUdid = interfaces.get(Constant.ROUTER_HEADER_IMEI_NO);
		this.deviceMake = interfaces.get(Constant.ROUTER_HEADER_DEVICE_MAKE);
		this.deviceModel = interfaces.get(Constant.ROUTER_HEADER_DEVICE_MODEL);
		this.appVersionNo = interfaces.get(Constant.ROUTER_HEADER_APPLICATION_VERSION);
		this.installLatitude = interfaces.get(Constant.ROUTER_HEADER_DEVICE_LATITUDE);
		this.installLongitude = interfaces.get(Constant.ROUTER_HEADER_DEVICE_LONGITUDE);
	}
}
