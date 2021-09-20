package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;


@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage() {

        for (int i = 1; i <= 10; i++) {
            OrderReturnReasonEntity order = new OrderReturnReasonEntity();
            order.setId(1L);
            order.setName("hello+"+i);
            order.setSort(1);
            order.setStatus(0);
            order.setCreateTime(new Date());
            rabbitTemplate.convertAndSend("hello-java-exchage","hello.java",order);
            log.info("消息发送成功:{}",order.getName());
        }

    }


    /**
     * 1.如何创建 Exchange Queue Binding
     *  在使用 AmqpAdmin 进行创建
     *
     * 2.如何发送消息
     */
    @Test
    public void createExchange() {

        //DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        DirectExchange exchange = new DirectExchange("hello-java-exchage",true,false);
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange【{}】 创建成功","hello-java-exchage");

    }

    @Test
    public void createQueue(){
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments) {
        Queue queue = new Queue("hello-java-queue",true,false,false,null);
        amqpAdmin.declareQueue(queue);
        log.info("Exchange【{}】 创建成功","hello-java-queue");
    }

    @Test
    public void createBinding(){
        //Binding(String destination[目的地], Binding.DestinationType destinationType【目的地类型】,
        // String exchange【交换机】, String routingKey【路由键】, @Nullable Map<String, Object> arguments【自定义参数】) {
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchage",
                "hello.java",
                null
        );
        amqpAdmin.declareBinding(binding);
        log.info("Binding【{}】 创建成功", "hello-java-binding");
    }

    @Test
    public void deleteBinding(){
//        amqpAdmin.
    }

}
