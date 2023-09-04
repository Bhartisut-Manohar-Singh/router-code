package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseOutput {
    private String status;
    private String message;
}
