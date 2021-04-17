package com.fengye.springboot.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:  direct交换机类型采用routing key与Queue进行绑定，通过key不同一对一进行消息传递
 * @Author: fengye
 * @Date: 2021/4/16 14:29
 */
@Configuration
public class DirectRabbitConfig {
    //使用注入方式声明对应的Queue
    @Bean
    public Queue emailQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue("email.direct.queue", true);
    }
    @Bean
    public Queue smsQueue() {
        return new Queue("sms.direct.queue", true);
    }
    @Bean
    public Queue weixinQueue() {
        return new Queue("weixin.direct.queue", true);
    }

    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public DirectExchange directOrderExchange() {
        return new DirectExchange("direct_order_exchange", true, false);
    }

    //绑定关系：将队列和交换机绑定, 并设置用于匹配键：routingKey
    @Bean
    public Binding bindingFanout1() {
        return BindingBuilder
                .bind(weixinQueue())  //绑定哪个Queue
                .to(directOrderExchange())  //是哪个交换机
                .with("weixin");   //对应什么key
    }
    @Bean
    public Binding bindingFanout2() {
        return BindingBuilder.bind(smsQueue()).to(directOrderExchange()).with("sms");
    }

    @Bean
    public Binding bindingFanout3() {
        return BindingBuilder.bind(emailQueue()).to(directOrderExchange()).with("email");
    }
}
