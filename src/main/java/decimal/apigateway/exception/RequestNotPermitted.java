package decimal.apigateway.exception;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class RequestNotPermitted extends RuntimeException {
    private final String message;

    private LocalDateTime requestTimestamp;

    private Map<String, String> httpHeaders;

    public RequestNotPermitted(String message,LocalDateTime requestTimestamp, Map<String, String>  httpHeaders) {
        super(message);
        this.message = message;
        this.requestTimestamp = requestTimestamp;
        this.httpHeaders = httpHeaders;
    }

    public String getMessage() {
        return this.message;
    }
}

