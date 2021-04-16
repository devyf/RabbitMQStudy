package com.fengye.rabbitmq.simple;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description:
 * @Author: huang
 * @Date: 2021/4/14 16:47
 */
public class Consumer {
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
            channel.basicConsume("myqueue1", true, new DeliverCallback() {
                public void handle(String s, Delivery message) throws IOException {
                    System.out.println("开始接收消息");
                    System.out.println("收到的消息是：" + new String(message.getBody(), "UTF-8"));
                }
            }, new CancelCallback() {
                public void handle(String s) throws IOException {
                    System.out.println("接收消息失败！");
                }
            });
            System.in.read();  //暂停程序
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (
                TimeoutException e) {
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
