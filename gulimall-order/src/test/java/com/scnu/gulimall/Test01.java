package com.scnu.gulimall;

import com.alibaba.fastjson.TypeReference;
import com.scnu.common.utils.R;
import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.feign.ProductFeignService;
import com.scnu.gulimall.order.vo.SpuInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test01 {


    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Test
    public void testPro(){
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(1L);
        System.out.println(spuInfoBySkuId.getData("data",new TypeReference<SpuInfoEntity>(){}));
    }

    @Test
    public void testSendMessage(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setBillContent("222");
        orderEntity.setOrderSn("dsdsd");
        orderEntity.setCommentTime(new Date());
        rabbitTemplate.convertAndSend("hello.java.exchange","java.key",orderEntity);
    }

    @Test
    public void testExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        DirectExchange directExchange = new DirectExchange("hello.java.exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("directExchange[{}]创建成功-->",directExchange.getName());
    }

    @Test
    public void testQueue(){
        Queue queue = new Queue("java.queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("queue[{}]创建成功-->",queue.getName());
    }

    @Test
    public void testBinding(){
        Binding binding = new Binding(
                "java.queue",
                Binding.DestinationType.QUEUE,
                "hello.java.exchange",
                "java.key",null);
        amqpAdmin.declareBinding(binding);
        log.info("binding->key:[{}]创建成功-->",binding.getRoutingKey());
    }


}
