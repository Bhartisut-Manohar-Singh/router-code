package decimal.apigateway;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAspectJAutoProxy
@EnableAsync
@EnableSwagger2
@ComponentScan(basePackages = { "decimal.apigateway","decimal.common.micrometer"})
public class ApiGatewayApplication {


    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }


    @Value("${maxConnectionPerRoute}")
    int maxConnectionPerRoute;

    @Value("${maxHttpConnections}")
    int maxHttpConnections;

    @Value("${connectionTimeout}")
    int connectionTimeout;

    @Value("${readTimeout}")
    int readTimeout;
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate()
    {
        RestTemplate template = new RestTemplate();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxHttpConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionPerRoute);

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();


        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setConnectTimeout(connectionTimeout);
        requestFactory.setHttpClient(httpClient);

        template.setRequestFactory(requestFactory);

        return template;
    }
}
