package decimal.apigateway.model;

import decimal.logs.model.RequestIdentifier;
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



    public RequestIdentifier getRequestIdentifier(Request request){
        RequestIdentifier requestIdentifier = new RequestIdentifier();
        requestIdentifier.setLoginId(request.getLoginId());
        requestIdentifier.setOrgId(request.getOrgId());
        requestIdentifier.setAppId(request.getAppId());
        requestIdentifier.setTraceId(request.getTraceId());
        requestIdentifier.setSystemName("SECURITY");
        requestIdentifier.setArn(request.getServiceName());

        return requestIdentifier;
    }

}
