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
public class DeadRabbitMqConfiguration {
    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public DirectExchange deadDirectExchange() {
        return new DirectExchange("dead_direct_exchange", true, false);
    }

    //定义队列的过期时间
    @Bean
    public Queue deadQueue() {
        return new Queue("dead.direct.queue", true, false, false);
    }

    @Bean
    public Binding deadBingding(){
        return BindingBuilder.bind(deadQueue()).to(deadDirectExchange()).with("dead");
    }

}
