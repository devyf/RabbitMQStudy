package com.fengye.rabbitmq.all;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @Description:
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
            //3.从连接工厂中获取连接
            connection = factory.newConnection("生产者");
            //4.通过连接获取通道Channel
            channel = connection.createChannel();
            //5.准备发送消息的内容
            String message = "测试全代码创建交换机路由Queue！";
            //交换机名称
            String exchangeName = "topic_message_exchange";
            //交换机类型  direct/topic/fanout/headers
            String exchangeType = "topic";

            //队列名称
            String queue1Name = "queue5";
            String queue2Name = "queue6";
            String queue3Name = "queue7";
            String queue4Name = "queue8";

            //6.声明交换机
            channel.exchangeDeclare(exchangeName, exchangeType, true, false, null);
            //7.声明队列
            channel.queueDeclare(queue1Name, true, false, false, null);
            channel.queueDeclare(queue2Name, true, false, false, null);
            channel.queueDeclare(queue3Name, true, false, false, null);
            channel.queueDeclare(queue4Name, true, false, false, null);
            //8.绑定交换机与队列的关系
            channel.queueBind(queue1Name, exchangeName, "com.#");
            channel.queueBind(queue2Name, exchangeName, "*.course.*");
            channel.queueBind(queue3Name, exchangeName, "#.order.#");
            channel.queueBind(queue4Name, exchangeName, "#.user.*");
            //9.发送消息给中间件rabbitmq-server
            /**
             * @params1: 交换机  @params2：队列、路由key   @params3：消息的状态控制  @params4：消息主题
             * 队列虽然没有给指定的交换机，但是一定会存在一个默认的交换机（simple这里做简单操作，暂时不指定交换机）
             */
            String routeKey = "com.course.order";  //发给queue 5/6/7
            channel.basicPublish(exchangeName, routeKey, null, message.getBytes());
            System.out.println("消息发送成功！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送消息出现异常" + e.getMessage());
        } finally {
            //7: 释放连接关闭通道
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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
