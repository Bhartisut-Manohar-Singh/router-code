package decimal.apigateway.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Log
@Component
public class MultipartUtil {

    private RestTemplate template;

    @Value("${maxConnectionPerRoute}")
    int maxConnectionPerRoute;

    @Value("${maxHttpConnections}")
    int maxHttpConnections;

    @Value("${connectionTimeout}")
    int connectionTimeout;

    @Value("${readTimeout}")
    int readTimeout;


    @Autowired
    ObjectMapper objectMapper;

    public void enableConnectionPool() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxConnectionPerRoute);
        connectionManager.setMaxTotal(maxHttpConnections);

        CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        template.setRequestFactory(requestFactory);
    }

    @Override
    public ResponseEntity<Object> executeHttpRequest(RequestEntity<Object> request) {
        Object requestBody = request.getBody();
        HttpMethod requestMethod = request.getMethod();
        HttpHeaders headers = request.getHeaders();

        HttpEntity<String> entity = new HttpEntity<>(String.valueOf(requestBody), headers);

        return template.exchange(request.getUrl(), requestMethod, entity, Object.class);
    }

    @Override
    @Timed
    public ResponseEntity<Object> executeHttpRequest(HttpHeaders headers, Object requestBody, String url, String requestMethod) {
        //headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(String.valueOf(requestBody), headers);
        try {
            log.info("======= calling post for url ===== " + url + " with body " + objectMapper.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return template.exchange(url, HttpMethod.valueOf(requestMethod), entity, Object.class);
    }

    @Override
    public ResponseEntity<byte[]> executeMultiPartRequest(HttpHeaders headers, Object requestBody, String url, String requestMethod, int timeOut) {
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
        RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.ofMilliseconds(timeOut)).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(timeOut * 1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);


        switch (requestMethod) {

            case "GET":
                return restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            default:
                return restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        }
    }

}
