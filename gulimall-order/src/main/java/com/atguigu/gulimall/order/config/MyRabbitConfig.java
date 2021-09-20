package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author zero
 * @create 2020-10-06 21:19
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就回调
     *     1、spring.rabbitmq.publisher-confirm-type=correlated
     *     2、设置确认回调
     * 2、消息正确抵达队列进行回调
     *     1、spring.rabbitmq.publisher-returns=true
     *        spring.rabbitmq.template.mandatory=true
     *     2、设置确认回调
     *
     * 3、消费端确认(保证每个消息被正确消费，此时才可以broker删除这个消息)
     *     1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：
     *              收到很多消息，自动回复给服务器ack，只有一个消息处理成功，然后宕机了，消息回丢失
     *              消费者手动确认签收模式，只要没有明确的告诉MQ，货物被签收，没有Ack
     *              消息就会一直unacked状态，即使consumer 宕机，消息不会丢失，重新变为ready
     *    2.如何签收：
     *      channel.basicAck(deliveryTag,false); 签收
     *      channel.basicNack(deliveryTag,false,false); 拒签
     *
     *
     * @PostConstruct:构造器创建完成后执行这个方法
     */
    @PostConstruct //MyRabbitConfig 对象创建完成后，执行改方法
    public void initRabbitTemplate(){
        //消息成功抵达exchange回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *1、只要消息抵达Broker就ack=true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm.....correlationData["+correlationData+"]==>ack["+ack+"]==>cause["+cause+"]");
            }
        });

        //设置消息抵达队列的失败确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就会触发这个失败回调
             * @param message   投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange  当时这个消息发送给那个交换机
             * @param routeKey  当时这个消息用那个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routeKey) {
                System.out.println("returnedMessage==>message["+message+"]==>replyCode["+replyCode+"]==>replyText["+replyText+"]==>exchange["+exchange+"]==>routeKey["+routeKey+"]");
            }
        });
    }


}
