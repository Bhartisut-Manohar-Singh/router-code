package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MicroserviceResponse {

    String status;
    String message;
    Object response;

    Map<String, String> customData;

    public MicroserviceResponse(String status, String message, Object response) {
        this.status = status;
        this.message = message;
        this.response = response;
    }

    @Override
    public String toString() {
        return "MicroserviceResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", response=" + response +
                ", customData=" + customData +
                '}';
    }
}
