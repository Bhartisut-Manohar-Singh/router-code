package decimal.apigateway.configuration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;


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

        //For Setting TimeOut
        //RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.of(Duration.ofMillis(readTimeout))).build();
        RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS).build();

        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        //requestFactory.setReadTimeout(readTimeout); This has been replaced by RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.of(Duration.ofMillis(readTimeout))).build();
        requestFactory.setConnectTimeout(connectionTimeout);
        requestFactory.setHttpClient(closeableHttpClient);
        template.setRequestFactory(requestFactory);
        return template;
    }
}
