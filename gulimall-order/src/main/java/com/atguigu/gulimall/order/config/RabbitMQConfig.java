package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zero
 * @create 2020-10-11 13:48
 */
@Configuration
public class RabbitMQConfig {

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(Message message, Channel channel, OrderEntity orderEntity){
//        System.out.println("收到过期消息："+ orderEntity.getOrderSn());
//        try {
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 在 springboot 中可以使用 @Bean 注解方式自动创建 Exchange Queue Binding
     * 前提：必须有监听队列时才会自动创建，没有监听就不会创建，RabbitMQ 没有这些 exchange queue binding
     *
     * 一旦创建好再次启动即使属性发生变化也不会覆盖 RabbitMQ 中创建好里的东西
     *
     */

    @Bean
    public Exchange orderEventExchange(){
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange",true,false,null);
    }

    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange"); //死信交换机
        arguments.put("x-dead-letter-routing-key","order.release.order"); //路由键
        arguments.put("x-message-ttl",60000); //过期时间
        //(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue("order.release.order.queue",true,false,false,null);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        //String destination, Binding.DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments)
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.other.#",null);
    }

    @Bean
    public Queue orderSeckillOrderQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue("order.seckill.order.queue",true,false,false,null);
    }

    @Bean
    public Binding orderseckillOrderBinding(){
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.seckill.order",null);
    }




}
