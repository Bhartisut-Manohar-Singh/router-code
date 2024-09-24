package decimal.apigateway.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PublicTokenCreationException extends Exception {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "errorCode")
    private String errorCode;
    @JsonProperty(value = "errorMessage")
    private String errorMessage;

    public PublicTokenCreationException(String errorCode, String message, Exception e) {
        this.errorCode = errorCode;
        this.errorMessage = message;
    }
}
