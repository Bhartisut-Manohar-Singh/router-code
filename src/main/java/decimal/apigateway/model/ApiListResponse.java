package decimal.apigateway.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiListResponse {

    private String id;
    @Indexed
    private String apiName;
    private String apiGrantedBy;
    private float apiVersion;
}