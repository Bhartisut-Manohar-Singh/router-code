package decimal.apigateway.model;

import lombok.*;

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

    public MicroserviceResponse(decimal.sessionmanagement.model.MicroserviceResponse microserviceResponse) {
        this.status = microserviceResponse.getStatus();
        this.message = microserviceResponse.getMessage();
        this.response = microserviceResponse.getResponse();
        this.customData = microserviceResponse.getCustomData();
    }

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
