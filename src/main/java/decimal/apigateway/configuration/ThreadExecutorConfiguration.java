package decimal.apigateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadExecutorConfiguration {

    @Value("${timeToLive}")
    int timeToLive;

    @Value("${corePoolSize}")
    int corePoolSize;

    @Value("${maxPoolSize}")
    int maxPoolSize;

    @Value("${queueCapacity}")
    int queueCapacity;

    @Bean(name = "myTaskExecutor")
    public TaskExecutor myTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(timeToLive);
        executor.setAllowCoreThreadTimeOut(true);

        return executor;
    }
}
