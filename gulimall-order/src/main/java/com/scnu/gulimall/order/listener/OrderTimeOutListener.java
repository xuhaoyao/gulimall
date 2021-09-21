package com.scnu.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = {"order.release.order.queue"})
@Component
public class OrderTimeOutListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void orderTryCancel(Message message, OrderEntity orderEntity, Channel channel) throws IOException {
        System.out.println("接收到订单过期消息...判断是否要取消订单");
        try {
            orderService.orderTryCancel(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
