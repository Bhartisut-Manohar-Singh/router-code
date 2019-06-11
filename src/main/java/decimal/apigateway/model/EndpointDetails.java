package decimal.apigateway.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.sql.Timestamp;

@SuppressWarnings("WeakerAccess")
@Component
@RequestScope
@Getter
@Setter
@NoArgsConstructor
public class EndpointDetails {

    private String url;
    private Timestamp requestTime;
    private Timestamp responseTime;
    private String executionTimeMs;
    private String type;
    private JsonNode headers;
    private JsonNode request;
    private JsonNode response;
    private String responseCode;
    private String responseStatus;
    private Object otherInfo;
}
