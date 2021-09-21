package com.scnu.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.scnu.common.to.mq.StockLockedTo;
import com.scnu.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = {"stock.release.stock.queue"})
@Component
public class UnLockedStockListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void releaseStock(Message message, StockLockedTo to, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息....");
        try {
            wareSkuService.releaseStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //远程调用查询订单状态可能失败,这里接住异常,让消息重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void releaseStock(Message message, String orderSn, Channel channel) throws IOException {
        System.out.println("订单取消了的解锁库存的消息....");
        try {
            wareSkuService.releaseStock(orderSn);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //远程调用查询订单状态可能失败,这里接住异常,让消息重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
