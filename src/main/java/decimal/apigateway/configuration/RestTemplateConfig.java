package decimal.apigateway.configuration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

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
