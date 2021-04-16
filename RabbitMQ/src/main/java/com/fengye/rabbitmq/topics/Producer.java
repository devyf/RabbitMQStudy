package com.fengye.rabbitmq.topics;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description:topic模式，已先通过界面与交换机绑定了每个queue的关系
 *  * queue1
 *  * com.#
 *  * queue2
 *  * *.course.*
 *  * queue3
 *  * #.order.#
 *  * queue4
 *  * #.user.*
 * @Author: huang
 * @Date: 2021/4/14 15:22
 */
public class Producer {
    public static void main(String[] args) {
        //所有的中间件技术都是基于tcp/ip协议基础之上构建新型的协议规范，只不过rabbitmq遵循的是amqp
        //1.创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("139.155.203.191");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");
        //2.创建连接Connection
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection("生产者");
            //3.通过连接获取通道Channel
            channel = connection.createChannel();
            //4.准备消息内容
            String message = "Hello Topic Queue!";
            //5.准备交换机，声明创建交换机信息
            String exchangeName = "topic_exchange";
            String type = "topic";
            boolean durable = true;
            String routeKey = "com.course.test.order";
            channel.exchangeDeclare(exchangeName, type, durable);
            //7.发送消息给队列queue
            /**
             * @params1: 交换机  @params2：队列、路由key   @params3：消息的状态控制  @params4：消息主题
             * 队列虽然没有给指定的交换机，但是一定会存在一个默认的交换机
             */
            channel.basicPublish(exchangeName, routeKey, null, message.getBytes());
            System.out.println("消息发送成功！");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            //7.关闭通道
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //8.关闭连接
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
