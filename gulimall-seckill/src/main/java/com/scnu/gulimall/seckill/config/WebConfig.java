package com.scnu.gulimall.seckill.config;

import com.scnu.gulimall.seckill.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    public WebConfig(UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 应当将用于秒杀业务的controller单独抽取出来,在类上标注
         * @RestController("seckill") 单独拦截这个controller比较合适
         * 这里由于前期没考虑到,不改了O(∩_∩)O
         */
        registry.addInterceptor(userInterceptor).addPathPatterns("/seckill/**");
    }
}
