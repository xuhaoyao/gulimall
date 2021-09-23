package com.scnu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @EnableFeignClients
 *  放在启动类上面不需要指定包
 *  如果放在自己定义的配置类里面,必须指定包名,否则扫描不到
 */
@EnableFeignClients(basePackages = "com.scnu.gulimall.member.feign")
@EnableDiscoveryClient
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){


        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //ThreadLocal传递此次请求的请求属性
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();  //老请求
                    //同步请求头
                    String cookie = request.getHeader("Cookie");
                    requestTemplate.header("Cookie", cookie);  //给远程调用的请求加上cookie
                }
            }
        };
    }

}
