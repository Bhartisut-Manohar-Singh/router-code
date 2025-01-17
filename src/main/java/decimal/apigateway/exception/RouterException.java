package decimal.apigateway.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import decimal.apigateway.commons.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouterException extends Exception {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "errorCode")
    private String errorCode;
    @JsonProperty(value = "errorMessage")
    private String errorMessage;
    private String messageTrace;
    private Object response;
    private String errorType;
    private String errorHint;


    public RouterException(String errorCode, String routerErrorTypeSecurity, String signatureVerificationFailed, String signature_verification_failed, Exception e) {
        this.errorCode = errorCode;
//        this.errorMessage = RouterOperationsV1.getMessageFromCode(errorCode);
        this.messageTrace = e != null ? e.getMessage() : "";
    }


    public RouterException(String errorCode, Exception e) {
        this.errorCode = errorCode;
//        this.errorMessage = RouterOperations.getMessageFromCode(errorCode);
        this.messageTrace = e != null ? e.getMessage() : "";
    }

    public RouterException(String errorCode, String message, Exception e) {
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.messageTrace = e != null ? e.getMessage() : "";
    }

    public RouterException(String errorCode, Exception e, String errorType, String errorHint) {
        this(errorCode, Constant.ROUTER_ERROR_TYPE_SECURITY, "Signature verification failed", "Signature verification failed", e);
        this.errorType = errorType;
        this.errorHint = errorHint;
    }

    public RouterException(String errorCode, String msg, String errorType, String errorHint) {
        this.errorCode = errorCode;
        this.errorMessage = msg;
        this.errorType = errorType;
        this.errorHint = errorHint;
    }

    public RouterException(String errorCode, String errorMessage, Object detailedLogs) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.response = detailedLogs;
    }

    public RouterException(Object response) {
        this.response = response;
    }

    public RouterException(String errorCode, Exception e, String errorType, String errorHint,Object response) {
        this(errorCode, Constant.ROUTER_ERROR_TYPE_SECURITY, "Signature verification failed", "Signature verification failed", e);
        this.errorType = errorType;
        this.errorHint = errorHint;
        this.response = response;
    }
}
