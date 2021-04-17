package com.fengye.springboot.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:  fanout交换机类型就是对应的消息采用广播订阅模式，订阅绑定交换机的队列都应该收到消息
 * @Author: fengye
 * @Date: 2021/4/16 14:29
 */
@Configuration
public class FanoutRabbitConfig {
    //使用注入方式声明对应的Queue
    @Bean
    public Queue emailQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue("email.fanout.queue", true);
    }
    @Bean
    public Queue smsQueue() {
        return new Queue("sms.fanout.queue", true);
    }
    @Bean
    public Queue weixinQueue() {
        return new Queue("weixin.fanout.queue", true);
    }

    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public FanoutExchange fanoutOrderExchange() {
        return new FanoutExchange("fanout_order_exchange", true, false);
    }

    //绑定关系：将队列和交换机绑定, 并设置用于匹配键：routingKey
    @Bean
    public Binding bindingFanout1() {
        return BindingBuilder
                .bind(weixinQueue())  //绑定哪个Queue
                .to(fanoutOrderExchange());  //是哪个交换机
    }
    @Bean
    public Binding bindingFanout2() {
        return BindingBuilder.bind(smsQueue()).to(fanoutOrderExchange());
    }

    @Bean
    public Binding bindingFanout3() {
        return BindingBuilder.bind(emailQueue()).to(fanoutOrderExchange());
    }
}
