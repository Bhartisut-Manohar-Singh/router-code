package decimal.apigateway.configuration;


import decimal.logs.model.CustomPayload;
import decimal.apigateway.enums.Headers;
import decimal.logs.filters.IdentifierFilter;
import decimal.logs.filters.AuditTraceFilter;
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

    @Bean
    @RequestScope
    public AuditPayload auditPayload() {
        AuditPayload auditPayload = new AuditPayload();
        auditPayload.setRequest(new Request());
        auditPayload.setResponse(new Response());
        return auditPayload;

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
        requestIdentifierMapper.mapLogOrgIdWithHeaderKey(Headers.logorgid.name());
        requestIdentifierMapper.mapLogAppIdWithHeaderKey(Headers.logappid.name());
        return new IdentifierFilter(requestIdentifierMapper, isHttpTracingEnabled);
    }

    @Bean
    public AuditTraceFilter auditTraceFilter() {
        List<String> registeredUrls = new ArrayList<>();
        registeredUrls.add("logout");
        registeredUrls.add("forceLogout");
        registeredUrls.add("generate/SSOToken");
        registeredUrls.add("validate/SSOToken");
        registeredUrls.add("sso-login-details");

        return new AuditTraceFilter("api-gateway", isHttpTracingEnabled, registeredUrls);
    }

    @Bean
    @RequestScope
    public CustomPayload customPayload() {
        return new CustomPayload();
    }
}
