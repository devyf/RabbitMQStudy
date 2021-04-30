package com.fengye.springboot.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: huang
 * @Date: 2021/4/30 8:27
 */
@Configuration
public class TTLRabbitMqConfiguration {
    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public DirectExchange ttldirectOrderExchange() {
        return new DirectExchange("ttl_direct_exchange", true, false);
    }

    //定义队列的过期时间
    //定义队列的死信队列
    //死信队列的route key
    @Bean
    public Queue directttlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 5000);  //这里一定是int类型
        args.put("x-dead-letter-exchange", "dead_direct_exchange"); //这里与定义好的死信交换机进行绑定，死信交换机会去找死信队列
        args.put("x-dead-letter-routing-key", "dead");  //如果是fanout模式这里不需要route key
        args.put("x-max-length", 5); //设置每次给死信队列中发送消息的长度
        return new Queue("ttl.direct.queue", true, false, false, args);
    }

    @Bean
    public Binding ttlBingding(){
        return BindingBuilder.bind(directttlQueue()).to(ttldirectOrderExchange()).with("ttl");
    }

    //定义队列的过期时间 --定义一个普通队列，在外面设置过期时间
    @Bean
    public Queue directttlMessageQueue() {
        return new Queue("ttl.message.direct.queue", true, false, false);
    }

    @Bean
    public Binding ttlMsgBingding(){
        return BindingBuilder.bind(directttlMessageQueue()).to(ttldirectOrderExchange()).with("ttlmsg");
    }
}
