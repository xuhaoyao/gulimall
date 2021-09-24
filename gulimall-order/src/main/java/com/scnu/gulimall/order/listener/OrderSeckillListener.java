package com.scnu.gulimall.order.listener;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.scnu.common.to.mq.SeckillOrderTo;
import com.scnu.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = {"order.seckill.order.queue"})
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void seckillListener(Message message, SeckillOrderTo orderTo, Channel channel) throws IOException {
        try{
            orderService.handleSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}
