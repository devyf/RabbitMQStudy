package com.fengye.springboot.rabbitmq.service.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: huang
 * @Date: 2021/4/16 15:48
 */
//通过@RabbitListener绑定队列接收消息
@RabbitListener(queues = {"weixin.fanout.queue"})
@Component
public class FanoutDuanxinConsumer {
    //队列中的消息会通过@RabbitHandler注解注入到方法参数中，就可以获取到队列中的消息
    @RabbitHandler
    public void reviceMessage(String message){
        System.out.println("weixin fanout----接收到了订单信息是：->" + message);
    }
}
