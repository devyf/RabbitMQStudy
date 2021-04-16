package com.fengye.rabbitmq.simple;

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
            connection = factory.newConnection("生产者");
            //3.通过连接获取通道Channel
            channel = connection.createChannel();
            //4.通过创建交换机，声明队列，绑定关系、路由key,发送消息和接收消息
            String queueName = "myqueue1";  //队列名称
            /*
             *  如果队列不存在，则会创建
             *  Rabbitmq不允许创建两个相同的队列名称，否则会报错。
             *
             *  @params1： queue 队列的名称
             *  @params2： durable 队列是否持久化（非持久化队列会存盘，但是会随着重启服务器会丢失）
             *  @params3： exclusive 是否排他，即是否私有的，如果为true,会对当前队列加锁，其他的通道不能访问，并且连接自动关闭
             *  @params4： autoDelete 是否自动删除，当最后一个消费者断开连接之后是否自动删除消息。
             *  @params5： arguments 可以设置队列附加参数，设置队列的有效期，消息的最大长度，队列的消息生命周期等等。
             * */
            channel.queueDeclare(queueName, false, false, false, null);
            //5.准备消息内容
            String message = "Hello RabbitMQ!!!00000000";
            //6.发送消息给中间件rabbitmq-server
            /**
             * @params1: 交换机  @params2：队列、路由key   @params3：消息的状态控制  @params4：消息主题
             * 队列虽然没有给指定的交换机，但是一定会存在一个默认的交换机（simple这里做简单操作，暂时不指定交换机）
             */
            channel.basicPublish("", queueName, null, message.getBytes());
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
