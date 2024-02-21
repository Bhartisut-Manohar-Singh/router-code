package decimal.apigateway.exception;

public class RequestNotPermitted extends RuntimeException {
    private final String message;

    public RequestNotPermitted(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

