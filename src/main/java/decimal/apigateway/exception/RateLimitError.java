package decimal.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
public class RateLimitError {
    String message;
    String status;
    HttpStatus statusCode;
}
