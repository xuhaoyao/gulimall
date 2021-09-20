package com.scnu.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * RabbitMQ
 * 1.引入amqp场景
 *      spring-boot-starter-amqp
 *      RabbitAutoConfiguration生效
 * 2.给容器中自动配置了RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3.@EnableRabbit
 * 4.@RabbitListener:监听消息,类+方法上(监听哪些队列)
 *  / @RabbitHandler:标注在方法上(重载区分不同的消息)
 *
 *
 *  Seata控制分布式事务
 *  1)每一个微服务创建undo_log
 *  2)配置seata服务器
 *  3)整合
 *      1.导入依赖 spring-cloud-starter-alibaba-seata
 *              io.seata:seata-all:0.7.1 这里是0.7.1,那么就去github下载0.7.1的版本
 *      2.所有想要用到分布式事务的微服务都要配置seata的代理服务器,见SeataConfig
 */

@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
