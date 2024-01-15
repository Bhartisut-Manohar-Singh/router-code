package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Component
@RequestScope
public class Request {

    private String orgId;
    private String appId;
    private String serviceName;
    private String loginId;
    private String traceId;

}
