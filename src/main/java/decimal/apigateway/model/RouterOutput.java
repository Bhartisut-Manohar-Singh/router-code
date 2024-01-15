package decimal.apigateway.model;

import decimal.apigateway.exception.RouterError;
import decimal.apigateway.exception.RouterException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouterOutput {

	
	private List<ServiceOutput> serviceOutputList;
	private RouterError routerError;

	public RouterOutput(Exception e)
	{
		if(e instanceof RouterException)
		{        		
    	this.setRouterError(new RouterError(((RouterException) e).getErrorCode(), e));
		}
		else
		{
		this.setRouterError(new RouterError("ROUTER_API_EXECUTION",e));
		}
	}

	public List<ServiceOutput> getServiceOutputList() {
		return serviceOutputList;
	}

	public void setServiceOutputList(List<ServiceOutput> serviceOutputList) {
		this.serviceOutputList = serviceOutputList;
	}

	public RouterError getRouterError() {
		return routerError;
	}

	public void setRouterError(RouterError routerError) {
		this.routerError = routerError;
	}

	
}
