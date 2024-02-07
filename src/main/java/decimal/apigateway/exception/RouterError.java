package decimal.apigateway.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class RouterError implements Serializable{


	private static final long serialVersionUID = -2097030522038365329L;
	@JsonProperty(value="errorCode")
	private String code;
	@JsonProperty(value="errorMessage")
	private String message;
	private String messageTrace;
	private String errorType;
	private String errorHint;
	
    public RouterError(){}

	public RouterError(String code,Exception e)
	{
		this.code = code;
		this.messageTrace = e!=null?e.getMessage():"";
	}
	public RouterError(String code,String message,Exception e)
	{
		this.code = code;
		this.message = message;
		this.messageTrace = e!=null?e.getMessage():"";
	}

	public RouterError(String code, Exception e, String errorType, String errorHint)
	{
		this(code, e);
		this.errorHint = errorHint;
		this.errorType = errorType;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageTrace() {
		return messageTrace;
	}
	public void setMessageTrace(String messageTrace) {
		this.messageTrace = messageTrace;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getErrorHint() {
		return errorHint;
	}

	public void setErrorHint(String errorHint) {
		this.errorHint = errorHint;
	}
}
