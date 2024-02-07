package decimal.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ServiceDef {
    private static final long serialVersionUID = -3988011825305120987L;

    private String orgId;
    private String appId;
    private String name;
    private String status;
    private String isStopOnError;
    private String isAuditEnabled;
    private String isPayloadEncrypted = "N";
    private String isDigitallySigned = "N";
    private String scope;
    private String apiExecutionMode;
    private String keysToMask;
    private String logPurgeDays;

    @JsonProperty("id")
    public void setOrgIdAndAppId(Map<String, String> id)
    {
        this.orgId = id.get("orgId");
        this.appId = id.get("appId");

        System.out.println("OrgId found: " + orgId + " and appId: " + appId);
    }
}
