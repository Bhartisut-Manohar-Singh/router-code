package decimal.apigateway.model;

import decimal.apigateway.exception.RouterError;
import decimal.apigateway.exception.RouterException;

import java.io.Serializable;
import java.util.List;

public class ApiOutput implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4539226445316944142L;

	public ApiOutput() {}
	public ApiOutput(Exception e, String apiName)
	{
		if(e instanceof RouterException)
		{
			RouterException routerException = (RouterException) e;
    	this.setApiError(new RouterError(routerException.getErrorCode(), routerException, routerException.getErrorType(), routerException.getErrorHint()));
		}
		else
		{
		this.setApiError(new RouterError("ROUTER_API_EXECUTION", e));
		}
		this.setApiName(apiName);
	}
	private List<RecordOutput> recordOutput;
	private String apiName;
	private RouterError apiError;
	private String pwcTableName;
	private String responseHandledBy;
	private String responseHandleType;
	private String whereCondition;
	private Boolean successRuleValidation;

	public  List<RecordOutput> getRecordOutput() {
		return recordOutput;
	}
	public void setRecordOutput(List<RecordOutput> recordOutput) {
		this.recordOutput = recordOutput;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public RouterError getApiError() {
		return apiError;
	}
	public void setApiError(RouterError apiError) {
		this.apiError = apiError;
	}
	public String getPwcTableName() {
		return pwcTableName;
	}
	public void setPwcTableName(String pwcTableName) {
		this.pwcTableName = pwcTableName;
	}
	public String getResponseHandledBy() {
		return responseHandledBy;
	}
	public void setResponseHandledBy(String responseHandledBy) {
		this.responseHandledBy = responseHandledBy;
	}
	public String getResponseHandleType() {
		return responseHandleType;
	}
	public void setResponseHandleType(String responseHandleType) {
		this.responseHandleType = responseHandleType;
	}
	public String getWhereCondition() {
		return whereCondition;
	}
	public void setWhereCondition(String whereCondition) {
		this.whereCondition = whereCondition;
	}
	public Boolean getSuccessRuleValidation() {
		return successRuleValidation;
	}
	public void setSuccessRuleValidation(Boolean successRuleValidation) {
		this.successRuleValidation = successRuleValidation;
	}
	
}
