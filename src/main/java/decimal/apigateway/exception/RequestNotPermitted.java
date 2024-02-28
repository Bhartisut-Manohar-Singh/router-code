package decimal.apigateway.exception;

import org.springframework.http.HttpStatus;

public class RequestNotPermitted extends RuntimeException {
    private final String message;
    private final String status;
    private final HttpStatus statusCode;

    public RequestNotPermitted(String message,String status,HttpStatus statusCode) {
        super(message);
        this.message = message;
        this.status = status;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return this.message;
    }
    public String getStatus() {
        return this.status;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }
}

