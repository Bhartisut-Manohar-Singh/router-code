package decimal.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAspectJAutoProxy
@EnableAsync
@EnableSwagger2
@EnableAutoConfiguration(exclude = {
        org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration.class,
        org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration.class
})
@ComponentScan(basePackages = { "decimal.apigateway","decimal.logs.kafka"," decimal.logs.connector"})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
