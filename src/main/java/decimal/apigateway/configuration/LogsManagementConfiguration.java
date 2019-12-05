package decimal.apigateway.configuration;

import decimal.logs.filters.AuditTraceFilter;
import decimal.logs.model.AuditPayload;
import decimal.logs.model.ErrorPayload;
import decimal.logs.model.LogEntry;
import decimal.logs.model.Payload;
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
    public AuditTraceFilter auditTraceFilter() {
        List<String> registeredUrls = new ArrayList<>();
        registeredUrls.add("register");
        registeredUrls.add("gatewayProcessor");
        registeredUrls.add("execute");
        registeredUrls.add("logout");
        registeredUrls.add("forceLogout");
        registeredUrls.add("dynamic-router");


        return new AuditTraceFilter("API-GATEWAY", isHttpTracingEnabled, registeredUrls);
    }
}
