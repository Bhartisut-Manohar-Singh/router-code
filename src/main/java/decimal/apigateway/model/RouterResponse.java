package decimal.apigateway.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RouterResponse
{
	private String status;
	private String message;
	private ObjectNode response;
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String status)
	{
		this.status = status;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public ObjectNode getResponse()
	{
		return response;
	}
	public void setResponse(ObjectNode response)
	{
		this.response = response;
	}

	public RouterResponse(String status, String message, ObjectNode response)
	{
		this.status = status;
		this.message = message;
		this.response = response;
	}
	public RouterResponse() {}

}
