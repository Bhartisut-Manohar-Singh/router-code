package decimal.apigateway.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.sql.Timestamp;
import java.util.List;

@Component
@RequestScope
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogsData {
    private String orgId;
    private String appId;
    private String environment;
    private String loginId;
    private String resourceName;
    private String resourceVersion;
    private Timestamp requestTimeStamp;
    private Timestamp responseTimeStamp;
    private String responseCode;
    private String requestId;
    private String requestInterfaceIP;
    private String processedByServer;
    private float executionTimeMs;
    private String responseStatus;
    private ObjectNode request;
    private ObjectNode response;
    private ObjectNode requestHeaders;
    private ObjectNode responseHeaders;
    private String isLogsEnabledForService;
    private String isLogsEnabledForOrgApp;
    private String moduleName;
    private Object otherInfo;

    private List<EndpointDetails> endpointDetails;

    public LogsData(LogsData logsData) {
        this.orgId = logsData.getOrgId();
        this.appId = logsData.getAppId();
        this.environment = logsData.getEnvironment();
        this.loginId = logsData.getLoginId();
        this.resourceName = logsData.getResourceName();
        this.resourceVersion = logsData.getResourceVersion();
        this.requestTimeStamp = logsData.getRequestTimeStamp();
        this.responseTimeStamp = logsData.getResponseTimeStamp();
        this.responseCode = logsData.getResponseCode();
        this.requestId = logsData.getRequestId();
        this.requestInterfaceIP = logsData.getRequestInterfaceIP();
        this.processedByServer = logsData.getProcessedByServer();
        this.executionTimeMs = logsData.getExecutionTimeMs();
        this.responseStatus = logsData.getResponseStatus();
        this.request = logsData.getRequest();
        this.response = logsData.getResponse();
        this.isLogsEnabledForService = logsData.getIsLogsEnabledForService();
        this.requestHeaders = logsData.getRequestHeaders();
        this.responseHeaders = logsData.getResponseHeaders();
        this.moduleName = "api_gateway";
        this.otherInfo = logsData.getOtherInfo();
        this.endpointDetails = logsData.getEndpointDetails();
        this.isLogsEnabledForOrgApp=logsData.getIsLogsEnabledForOrgApp();
    }
}
