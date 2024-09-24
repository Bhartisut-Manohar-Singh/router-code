package decimal.apigateway.service.util;


import com.ecwid.consul.v1.health.model.HealthService;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Getter
@Setter
public class ServiceInstanceUtil {

   private String instanceId;
   private String host;
   private String serviceId;
   private String port;
   List<String> tags;
   private HealthService healthService;
   private MetaData metaData;

}
