package decimal.apigateway.model;

import decimal.apigateway.exception.RouterError;
import decimal.apigateway.exception.RouterException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceOutput {
    private List<ApiOutput> serviceOutputList;
    private String serviceName;
    private RouterError serviceError;

    public ServiceOutput() {
    }

    public ServiceOutput(String serviceName) {
        this.setServiceName(serviceName);
    }

    public ServiceOutput(Exception e, String serviceName) {
        if (e instanceof RouterException) {
            RouterException routerException = (RouterException) e;
            this.setServiceError(new RouterError(routerException.getErrorCode(), routerException, routerException.getErrorType(), routerException.getErrorHint()));
        } else {
            this.setServiceError(new RouterError("ROUTER_API_EXECUTION", e));
        }
        this.setServiceName(serviceName);
    }

    public List<ApiOutput> getServiceOutput() {
        return serviceOutputList;
    }

    public void setServiceOutput(List<ApiOutput> serviceOutput) {
        this.serviceOutputList = serviceOutput;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public RouterError getServiceError() {
        return serviceError;
    }

    public void setServiceError(RouterError serviceError) {
        this.serviceError = serviceError;
    }
}
