package com.scnu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisson(){
        // 默认连接地址 127.0.0.1:6379
        //RedissonClient redisson = Redisson.create();
        Config config = new Config();
        //config.useSingleServer().setAddress("redis://192.168.200.130:6379");
        config.useSingleServer().setAddress("redis://182.92.2.51:6379").setPassword("aa6631656");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

}
