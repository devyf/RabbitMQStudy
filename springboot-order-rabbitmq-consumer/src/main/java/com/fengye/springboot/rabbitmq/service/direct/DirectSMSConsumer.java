package com.fengye.springboot.rabbitmq.service.direct;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: huang
 * @Date: 2021/4/16 15:49
 */
@RabbitListener(queues = {"sms.direct.queue"})
//@Component
public class DirectSMSConsumer {
    @RabbitHandler
    public void reviceMessage(String message){
        System.out.println("sms direct----接收到了订单信息是：->" + message);
    }
}
