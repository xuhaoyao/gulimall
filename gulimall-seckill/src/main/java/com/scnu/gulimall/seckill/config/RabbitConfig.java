package com.scnu.gulimall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@EnableRabbit
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 消息类型转化为json
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 1.做好消息确认机制(publisher,consumer[手动ack])
     * 2.每一个发送的消息都要在数据库做好记录,定期将失败的消息再发一次
     *      消息序列化成json,保存消息的类型,便于下次的反序列化,保存这个消息的路由器,路由键,队列,id,以及消息状态
     *          0-新建 1-已发送 2-错误抵达 3-已抵达
     *          新建就是要发这个消息的时候,将消息插入数据库
     *          已发送是ConfirmCallback的回调,消息抵达了broker
     *          错误抵达是ReturnCallback的回调,消息没有抵达到指定的队列
     *          已抵达是消息被成功消费了
     *
     * CREATE TABLE `mq_message` (
     * `message_id` char(32) NOT NULL,
     * `content` text COMMENT '消息转成json存储,通过class_type逆转',
     * `to_exchane` varchar(255) DEFAULT NULL,
     * `routing_key` varchar(255) DEFAULT NULL,
     * `class_type` varchar(255) DEFAULT NULL,
     * `message_status` int(1) DEFAULT '0' COMMENT '0-新建 1-已发送 2-错误抵达 3-已抵达',
     * `create_time` datetime DEFAULT NULL,
     * `update_time` datetime DEFAULT NULL,
     * PRIMARY KEY (`message_id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
     *
     */
    @PostConstruct
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达broker ->  ack = true
             * @param correlationData
             * @param ack
             * @param cause
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData-->" + correlationData + ",ack--->" + ack + ",cause--->" + cause);
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 消息没有投递给指定队列,就会触发回调
             * @param message   丢失的消息
             * @param replyCode 状态码
             * @param replyText 丢失原因
             * @param exchange
             * @param routingKey
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("message-->" + message);
                System.out.println("replyCode-->" + replyCode + ",replyText-->" + replyText);
                System.out.println("exchange-->" + exchange + ",routingKey-->" + routingKey);
            }
        });
    }

}
