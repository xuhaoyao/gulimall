package com.scnu.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.entity.OrderReturnApplyEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.order.dao.OrderItemDao;
import com.scnu.gulimall.order.entity.OrderItemEntity;
import com.scnu.gulimall.order.service.OrderItemService;


@RabbitListener(queues = "java.queue")
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /**
     *
     * @param message 原生消息
     * @param content <T>发送消息的消息体
     * @param channel 当前传输数据的通道
     *
     * 消费端确认:
     *     spring.rabbitmq.listener.direct.acknowledge-mode=manual 手动确认
     * channel.basicAck(deliveryTag,false); 签收
     * channel.basicNack(deliveryTag,false,false); 拒签
     */
    @RabbitHandler
    public void receiveMessage(Message message, OrderEntity content, Channel channel){

        System.out.println("接受到消息-->" + message);
        System.out.println("消息内容--->" + content);
        System.out.println("通道--->" + channel);

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnApplyEntity content, Channel channel){

        System.out.println("接受到消息-->" + message);
        System.out.println("消息内容--->" + content);
        System.out.println("通道--->" + channel);
    }

}