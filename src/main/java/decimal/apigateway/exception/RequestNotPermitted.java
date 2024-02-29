package decimal.apigateway.exception;

import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpHeaders;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class RequestNotPermitted extends RuntimeException {
    private final String message;

    private Instant requestTimestamp;

    private Map<String, String> httpHeaders;

    public RequestNotPermitted(String message,Instant requestTimestamp, Map<String, String>  httpHeaders) {
        super(message);
        this.message = message;
        this.requestTimestamp = requestTimestamp;
        this.httpHeaders = httpHeaders;
    }

    public String getMessage() {
        return this.message;
    }
}

