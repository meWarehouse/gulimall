package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zero
 * @create 2020-10-11 14:58
 */
@Configuration
public class MyRabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void listener(){
//
//    }

    @Bean
    public Exchange stockEventExchange() {
        //(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        return new TopicExchange("stock-event-exchange", true, false, null);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 120000);
        //(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        //stock.release.stock.queue
        return new Queue("stock.release.stock.queue", true, false, false, null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        //stock.release.#
        //(String destination, Binding.DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments) {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding() {
        //stock.locked
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);

    }


}
