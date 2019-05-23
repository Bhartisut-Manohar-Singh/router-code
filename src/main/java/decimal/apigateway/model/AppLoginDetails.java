package decimal.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppLoginDetails {

	private String requestId;

	private String loginId;

	private String imeiNo;

	private Timestamp appLastLoginTimestamp;
	
	private String appLastLoginLatitude;
	
	private String appLastLoginLongitude;

	@JsonProperty("interfaces")
	public void getInterfaces(Map<String, String> interfaces)
	{
		this.imeiNo = interfaces.get("IMEI_NO");
		this.appLastLoginLatitude = interfaces.get("DEVICE_LATITUDE");
		this.appLastLoginLongitude = interfaces.get("DEVICE_LONGITUDE");
		this.appLastLoginTimestamp = new Timestamp(System.currentTimeMillis());
	}
}
