package com.scnu.gulimall.order.config;

import com.scnu.gulimall.order.properties.ThreadPoolProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@EnableConfigurationProperties({ThreadPoolProperties.class})
@Configuration
public class ThreadPoolConfig {

    private final ThreadPoolProperties properties;

    public ThreadPoolConfig(ThreadPoolProperties properties){
        this.properties = properties;
    }

    @Bean
    public ExecutorService pool(){
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaximumPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getQueueSize()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
                );
    }

}
