package decimal.apigateway.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import decimal.apigateway.commons.Constant;
import decimal.apigateway.model.LogsData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiLogFormatter {

    private String orgId;
    private String appId;
    private String moduleName;
    private String actionType;
    private ObjectNode actionCriteria;
    private ObjectNode data;

    public ApiLogFormatter(LogsData logsData){
        this.orgId=logsData.getOrgId();
        this.appId=logsData.getAppId();
        this.moduleName=logsData.getModuleName();
        this.actionType = Constant.SAVE;
    }
}
