package com.scnu.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

//    @RabbitListener(queues = {"stock.release.stock.queue"})
//    public void initTest(Message message){
//
//    }

    /**
     * 消息类型转化为json
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue",true,false,false,null);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release.#", null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked", null);
    }

    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
    }

}
