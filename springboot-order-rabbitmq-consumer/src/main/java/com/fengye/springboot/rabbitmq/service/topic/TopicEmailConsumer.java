package com.fengye.springboot.rabbitmq.service.topic;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: huang
 * @Date: 2021/4/16 18:41
 */
@RabbitListener(bindings = @QueueBinding(
        //队列名字，绑定对应的队列接收消息
        value = @Queue(value = "email.topic.queue", autoDelete = "false"),
        //交换机名字，必须和生产者中交换机名相同；指定绑定的交换机类型
        exchange = @Exchange(value = "topic_order_exchange", type = ExchangeTypes.TOPIC),
        key = "#.order.#"
))
@Component
public class TopicEmailConsumer {
    //队列中的消息会通过@RabbitHandler注解注入到方法参数中，就可以获取到队列中的消息
    @RabbitHandler
    public void reviceMessage(String message){
        System.out.println("email topic----接收到了订单信息是：->" + message);
    }

}
