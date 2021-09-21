package com.scnu.gulimall.order.web;

import com.scnu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page){
        return page;
    }

    @ResponseBody
    @GetMapping("/test/delayMQ")
    public String testDelay(){
        OrderEntity order = new OrderEntity();
        order.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order);
        return "ok";
    }

}
