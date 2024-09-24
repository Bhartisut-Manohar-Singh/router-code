package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequest {

    private String orgId;
    private String appId;
    private Object apiData;
    private String loginId;
    private String installationId;
    private String status;

    public AnalyticsRequest(InstallationDetails installationDetails){
        this.orgId=installationDetails.getOrgId();
        this.appId=installationDetails.getAppId();
        this.apiData=installationDetails;
        this.loginId="";
        this.installationId=installationDetails.getInstallationId();
        this.status="";
    }

    public AnalyticsRequest(LastLoginDetails lastLoginDetails, String status){
        this.orgId= lastLoginDetails.getOrgId();
        this.appId= lastLoginDetails.getAppId();
        this.apiData= lastLoginDetails;
        this.loginId= lastLoginDetails.getLoginId();
        this.installationId= lastLoginDetails.getInstallationId();
        this.status=status;
    }

    public AnalyticsRequest(SummaryDetails summaryDetails, String status){
        this.orgId=summaryDetails.getOrgId();
        this.appId=summaryDetails.getAppId();
        this.apiData=summaryDetails;
        this.installationId="";
        this.loginId=summaryDetails.getLoginId();
        this.status=status;
    }
}
