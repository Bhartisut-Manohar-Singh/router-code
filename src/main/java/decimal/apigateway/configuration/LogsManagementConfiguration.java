package decimal.apigateway.configuration;

import decimal.logs.connector.LogsConnector;
import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.filters.IdentifierFilter;
import decimal.logs.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class LogsManagementConfiguration {


    @Value("${isHttpTracingEnabled}")
    boolean isHttpTracingEnabled;

    @Value("logging-agent-url")
    String lsvUrl;

    @Bean
    @RequestScope
    public AuditPayload auditPayload() {
        return new AuditPayload();
    }

    @Bean
    @RequestScope
    public LogEntry logEntry() {
        return new LogEntry();
    }

    @Bean
    @RequestScope
    public Payload payload() {
        return new Payload();
    }

    @Bean
    @RequestScope
    public ErrorPayload errorPayload() {
        return new ErrorPayload();
    }

    @Bean
    public IdentifierFilter identifierFilter(){
        RequestIdentifierMapper requestIdentifierMapper = new RequestIdentifierMapper();
        requestIdentifierMapper.mapArnWithHeaderKey("servicename");
        requestIdentifierMapper.mapTraceIdWithHeaderKey("requestid");
        requestIdentifierMapper.mapOrgIdWithHeaderKey("orgid");
        requestIdentifierMapper.mapAppIdWithHeaderKey("appid");
        return new IdentifierFilter(requestIdentifierMapper, isHttpTracingEnabled);
    }

    @Bean
    public AuditTraceFilter auditTraceFilter() {
        List<String> registeredUrls = new ArrayList<>();
        registeredUrls.add("gatewayProcessor");
        registeredUrls.add("execute");
        registeredUrls.add("logout");
        registeredUrls.add("forceLogout");
        registeredUrls.add("dynamic-router");


        return new AuditTraceFilter("API-GATEWAY", isHttpTracingEnabled, registeredUrls, lsvUrl);
    }

    @Bean
    public LogsConnector logsConnector(){
        return  LogsConnector.newInstance(lsvUrl);
    }
}
