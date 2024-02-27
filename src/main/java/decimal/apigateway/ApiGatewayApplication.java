package decimal.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
/*import springfox.documentation.swagger2.annotations.EnableSwagger2;*/

@SpringBootApplication(exclude = {ServiceRegistryAutoConfiguration.class, AutoServiceRegistrationAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@EnableAspectJAutoProxy
@EnableAsync
//@EnableSwagger2
@ComponentScan(basePackages = { "decimal.apigateway","decimal.logs.kafka"," decimal.logs.connector", "decimal.ratelimiter"})
@EnableRedisRepositories(basePackages = {"decimal.ratelimiter.repo","decimal.apigateway.repository"})

public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
