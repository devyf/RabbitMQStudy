## 1.RabbitMQ过期时间TTL及死信队列

### 1.1.TTL概述

过期时间TTL表示可以对消息设置预期的时间，在这个时间内都可以被消费者接收获取；过了之后消息将自动被删除。RabbitMQ可以对**消息和队列**设置TTL。目前有两种方法可以设置。

- 第一种方法是通过队列属性设置，队列中所有消息都有相同的过期时间。
- 第二种方法是对消息进行单独设置，每条消息TTL可以不同。

注意：

如果上述两种方法同时使用，则消息的过期时间以两者之间TTL较小的那个数值为准。消息在队列的生存时间一旦超过设置的TTL值，就称为dead message被投递到死信队列， 消费者将无法再收到该消息。



界面具体设置如下图所示：

![image-20210430114419938](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430114419938.png)



### 1.2.TTL简单实现

①基于队列属性进行设置：

这里在springBoot-order-rabbitmq-producer项目中config目录新建一个TTLRabbitMqConfiguration，声明ttl交换机与ttlQueue，代码如下：

```java
@Configuration
public class TTLRabbitMqConfiguration {
    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public DirectExchange ttldirectOrderExchange() {
        return new DirectExchange("ttl_direct_exchange", true, false);
    }

    //定义队列的过期时间
    @Bean
    public Queue directttlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 5000);  //这里一定是int类型
        return new Queue("ttl.direct.queue", true, false, false, args);
    }

    @Bean
    public Binding ttlBingding(){
        return BindingBuilder.bind(directttlQueue()).to(ttldirectOrderExchange()).with("ttl");
    }
}
```



在OrderService中进行消息发送至消息队列：

```java
@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 1: 定义交换机
    private String exchangeName = "";
    // 2: 路由key
    private String routeKey = "";


    //ttl--死信队列
    public void makeOrderTTLQueue(Long userId, Long productId, int num) {
        exchangeName = "ttl_direct_exchange";
        routeKey = "ttl";
        // 1： 模拟用户下单
        String orderNumer = UUID.randomUUID().toString();
        // 2: 根据商品id productId 去查询商品的库存
        // int numstore = productSerivce.getProductNum(productId);
        // 3:判断库存是否充足
        // if(num >  numstore ){ return  "商品库存不足..."; }
        // 4: 下单逻辑
        // orderService.saveOrder(order);
        // 5: 下单成功要扣减库存
        // 6: 下单完成以后
        System.out.println("用户 " + userId + ",订单编号是：" + orderNumer);
        // 发送订单信息给RabbitMQ fanout
        rabbitTemplate.convertAndSend(exchangeName, routeKey, orderNumer);
    }
}
```



进行测试：

```java
@SpringBootTest
class RabbitmqApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    void ttlQueueTest() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            Long userId = 100L + i;
            Long productId = 10001L + i;
            int num = 1;
            orderService.makeOrderTTLQueue(userId, productId, num);
        }
    }
}
```

可以看到消息向队列中发送，但是5s之后消息会自动从队列中移除，这就是TTL消息过期移除。

![image-20210430120101520](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430120101520.png)

![image-20210430120232325](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430120232325.png)



②基于某个消息发送时单独设置过期时间：

这种方式不需要在队列与交换机绑定时设置Queue过期属性，只需要声明为普通队列即可。

```java
@Configuration
public class TTLRabbitMqConfiguration {
    //声明交换机，不同的交换机类型不同：DirectExchange/FanoutExchange/TopicExchange/HeadersExchange
    @Bean
    public DirectExchange ttldirectOrderExchange() {
        return new DirectExchange("ttl_direct_exchange", true, false);
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
```



在发送时进行单独消息过期时间属性设置：

```java
@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 1: 定义交换机
    private String exchangeName = "";
    // 2: 路由key
    private String routeKey = "";

    //ttl--死信队列--普通队列设置
    public void makeOrderTTLMsgQueue(Long userId, Long productId, int num) {
        exchangeName = "ttl_direct_exchange";
        routeKey = "ttlmsg";

        String orderNumer = UUID.randomUUID().toString();

        System.out.println("用户 " + userId + ",订单编号是：" + orderNumer);

        //给消息设置过期时间
        MessagePostProcessor postProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("5000");  //时间为5s
                message.getMessageProperties().setContentEncoding("UTF-8");
                return message;
            }
        };

        // 发送订单信息给RabbitMQ fanout，指定消息的扩展信息
        rabbitTemplate.convertAndSend(exchangeName, routeKey, orderNumer, postProcessor);
    }
}
```



进行测试：

```java
@SpringBootTest
class RabbitmqApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    void ttlMsgQueueTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            Long userId = 100L + i;
            Long productId = 10001L + i;
            int num = 1;
            orderService.makeOrderTTLMsgQueue(userId, productId, num);
        }
    }
}
```

可以看到普通消息也可以通过设置过期时间，实现在消息队列中进行过期移除的功能。

![image-20210430121021139](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430121021139.png)

![image-20210430121032311](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430121032311.png)



①与②的主要区别在于：

通过队列设置ttl过期可以与死信队列进行绑定，后期过期之后可以加入死信队列；而通过单独普通消息后期设置属性无法加入到死信队列中，即没有备胎。

下面简单地使用SpringBoot方式实现一下死信队列。



### 1.3.死信队列

DLX，全称为Dead-Letter-Exchange , 可以称之为死信交换机，也有人称之为死信邮箱。当消息在一个队列中变成死信(dead message)之后，它能被重新发送到另一个交换机中，这个交换机就是DLX ，绑定DLX的队列就称之为死信队列。
消息变成死信，可能是由于以下的原因：

- 消息被拒绝
- 消息过期
- 队列达到最大长度

DLX也是一个正常的交换机，和一般的交换机没有区别，它能在任何的队列上被指定，实际上就是设置某一个队列的属性。当这个队列中存在死信时，Rabbitmq就会自动地将这个消息重新发布到设置的DLX上去，进而被路由到另一个队列，即死信队列。
要想使用死信队列，只需要在定义队列的时候设置队列参数 `x-dead-letter-exchange` 指定交换机即可。



死信队列的执行流程：

![img](https://kuangstudy.oss-cn-beijing.aliyuncs.com/bbs/2021/03/06/kuangstudy95eb209a-1bcd-487b-832a-e09d88da3beb.png)



### 1.4.死信队列简单实现

①在config目录下创建TTLRabbitMqConfiguration，声明ttl交换机及队列绑定关系，同时声明死信队列：

这里最主要的就是按照界面参数设置了死信队列exchange及routekey：

![image-20210430122359845](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430122359845.png)

```java
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
}
```



②业务层调用及测试：

```java
 //ttl--死信队列
public void makeOrderTTLQueue(Long userId, Long productId, int num) {
    exchangeName = "ttl_direct_exchange";
    routeKey = "ttl";
    String orderNumer = UUID.randomUUID().toString();

    System.out.println("用户 " + userId + ",订单编号是：" + orderNumer);
    // 发送订单信息给RabbitMQ fanout
    rabbitTemplate.convertAndSend(exchangeName, routeKey, orderNumer);
}
```

测试：

```java
  @Test
  void ttlQueueTest() throws InterruptedException {
      for (int i = 0; i < 5; i++) {
          Thread.sleep(1000);
          Long userId = 100L + i;
          Long productId = 10001L + i;
          int num = 1;
          orderService.makeOrderTTLQueue(userId, productId, num);
      }
  }
```

可以看到Queue属性中TTL、Lim相关的设置，5s过期后都加入到了死信队列中：

![image-20210430122700436](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430122700436.png)

![image-20210430122721131](C:\Users\huang\AppData\Roaming\Typora\typora-user-images\image-20210430122721131.png)



## 2.RabbitMQ内存管控

### 2.1.RibbitMQ持久化

持久化就把信息写入到磁盘的过程。

![img](https://kuangstudy.oss-cn-beijing.aliyuncs.com/bbs/2021/03/03/kuangstudy61cd63b0-fe19-4e07-9933-b87c3445df27.png)



RabbitMQ的持久化队列分为：
1：队列持久化
2：消息持久化
3：交换机持久化
不论是持久化的消息还是非持久化的消息都可以写入到磁盘中，只不过非持久的是等内存不足的情况下才会被写入到磁盘中。



### 2.2.RabbitMQ内存磁盘监控

**RabbitMQ的内存警告**

当内存使用超过配置的阈值或者磁盘空间剩余空间对于配置的阈值时，RabbitMQ会暂时阻塞客户端的连接，并且停止接收从客户端发来的消息，以此避免服务器的崩溃，客户端与服务端的心态检测机制也会失效。
如下图：

![img](https://kuangstudy.oss-cn-beijing.aliyuncs.com/bbs/2021/03/03/kuangstudy414d826e-5cea-4caa-aba2-92cd30be34f4.png)
当出现blocking或blocked话说明到达了阈值和以及高负荷运行了。

**RabbitMQ的内存控制**

参考帮助文档：https://www.rabbitmq.com/configure.html
当出现警告的时候，可以通过配置去修改和调整

①命令的方式

```
rabbitmqctl set_vm_memory_high_watermark <fraction>rabbitmqctl set_vm_memory_high_watermark absolute 50MB
```

fraction/value 为内存阈值。默认情况是：0.4/2GB，代表的含义是：当RabbitMQ的内存超过40%时，就会产生警告并且阻塞所有生产者的连接。通过此命令修改阈值在Broker重启以后将会失效，通过修改配置文件方式设置的阈值则不会随着重启而消失，但修改了配置文件一样要重启broker才会生效。

分析：

> rabbitmqctl set_vm_memory_high_watermark absolute 50MB

![img](https://kuangstudy.oss-cn-beijing.aliyuncs.com/bbs/2021/03/03/kuangstudy2a0177fc-9dd6-4285-8b26-1cc9cd0c6e35.png)

![img](https://kuangstudy.oss-cn-beijing.aliyuncs.com/bbs/2021/03/03/kuangstudy928db4f2-6860-470e-ba62-23e811eee586.png)

②配置文件方式 rabbitmq.conf

> 当前配置文件：/etc/rabbitmq/rabbitmq.conf

```bash
#默认
#vm_memory_high_watermark.relative = 0.4
# 使用relative相对值进行设置fraction,建议取值在04~0.7之间，不建议超过0.7.
vm_memory_high_watermark.relative = 0.6
# 使用absolute的绝对值的方式，但是是KB,MB,GB对应的命令如下：
vm_memory_high_watermark.absolute = 2GB
```

**RabbitMQ的内存换页**

在某个Broker节点及内存阻塞生产者之前，它会尝试将队列中的消息换页到磁盘以释放内存空间，持久化和非持久化的消息都会写入磁盘中，其中持久化的消息本身就在磁盘中有一个副本，所以在转移的过程中持久化的消息会先从内存中清除掉。

> 默认情况下，内存到达的阈值是50%时就会换页处理。
> 也就是说，在默认情况下该内存的阈值是0.4的情况下，当内存超过0.4*0.5=0.2时，会进行换页动作。

比如有1000MB内存，当内存的使用率达到了400MB,已经达到了极限，但是因为配置的换页内存0.5，这个时候会在达到极限400mb之前，会把内存中的200MB进行转移到磁盘中。从而达到稳健的运行。

可以通过设置 `vm_memory_high_watermark_paging_ratio` 来进行调整

```bash
vm_memory_high_watermark.relative = 0.4
vm_memory_high_watermark_paging_ratio = 0.7（设置小于1的值）
```

为什么设置小于1，以为你如果你设置为1的阈值。内存都已经达到了极限了。你在去换页意义不是很大了。

**RabbitMQ的磁盘预警**

当磁盘的剩余空间低于确定的阈值时，RabbitMQ同样会阻塞生产者，这样可以避免因非持久化的消息持续换页而耗尽磁盘空间导致服务器崩溃。

> 默认情况下：磁盘预警为50MB的时候会进行预警。表示当前磁盘空间第50MB的时候会阻塞生产者并且停止内存消息换页到磁盘的过程。
> 这个阈值可以减小，但是不能完全的消除因磁盘耗尽而导致崩溃的可能性。比如在两次磁盘空间的检查空隙内，第一次检查是：60MB ，第二检查可能就是1MB,就会出现警告。

通过命令方式修改如下：

```bash
rabbitmqctl set_disk_free_limit  <disk_limit>
rabbitmqctl set_disk_free_limit memory_limit  <fraction>
disk_limit：固定单位 KB MB GB
fraction ：是相对阈值，建议范围在:1.0~2.0之间。（相对于内存）
```

通过配置文件配置如下：

```bash
disk_free_limit.relative = 3.0
disk_free_limit.absolute = 50mb
```



