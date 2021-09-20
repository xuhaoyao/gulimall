package com.scnu.gulimall.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.scnu.gulimall.order.dao"})
public class MPConfig {
}
