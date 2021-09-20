package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author zero
 * @create 2020-10-16 13:09
 */
@Service
@Slf4j
@RabbitListener(queues = {"order.seckill.order.queue"})
public class SeckillOrderListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void handleSeckillOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {

       try{
           log.info("处理秒杀订单.......");
           orderService.handleSeckillOrder(orderTo);
           channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
       }catch(Exception e){
           channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
       }

    }

}
