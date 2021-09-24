package com.scnu.gulimall.seckill.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@EnableRedisHttpSession
@Configuration
public class SessionConfig {
    /**
     * 存入redis以json方式存入,否则每个存入session的类都需要序列化implements Serializable,否则报错
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericFastJsonRedisSerializer();
    }

    /**
     * cookieName默认是SESSION
     * 域名一定要写父域名
     * cookie的作用域是domain本身以及domain下的所有子域名。
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("GULIMALLCOOKIE");
        cookieSerializer.setDomainName("gulimall.com");
        return cookieSerializer;
    }

}