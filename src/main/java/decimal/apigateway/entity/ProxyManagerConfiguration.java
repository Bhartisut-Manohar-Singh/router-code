package decimal.apigateway.entity;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyManagerConfiguration implements ProxyManager{

    @Bean
    public ProxyManager<String> proxyManager(ProxyManager proxyManager,String key, BucketConfiguration bucketConfiguration) {
        // You can customize the ProxyManager configuration here
        ProxyManager<String> proxyManager = new ProxyManager<>(bucketConfiguration);

        return this.proxyManager(proxyManager);
    }

    @Override
    public RemoteBucketBuilder builder() {
        return null;
    }
}
