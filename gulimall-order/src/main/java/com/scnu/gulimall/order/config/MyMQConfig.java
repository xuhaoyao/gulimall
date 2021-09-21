package com.scnu.gulimall.order.config;

import com.rabbitmq.client.Channel;
import com.scnu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {


//    @RabbitListener(queues = {"order.release.order.queue"})
//    public void testListener(Message message, OrderEntity order, Channel channel) throws IOException {
//        System.out.println("收到订单消息--->" + order);
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        Queue orderDelayQueue = new Queue("order.delay.queue", true, false, false, arguments);
        return orderDelayQueue;
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateBingding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseBingding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

}
