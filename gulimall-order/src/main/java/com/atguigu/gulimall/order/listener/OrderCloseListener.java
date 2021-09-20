package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zero
 * @create 2020-10-11 22:42
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    /**
     * 能被此方法处理的必定是经过延时之后的订单
     *
     * @param message
     * @param channel
     * @param orderEntity
     * @throws IOException
     */
    @RabbitHandler
    public void listener(Message message, Channel channel, OrderEntity orderEntity) throws IOException {
        System.out.println("收到过期消息 .....："+ orderEntity.getOrderSn());
        try{
            //关闭订单
            orderService.closeOrder(orderEntity);
            // TODO 手动调用支付宝收单功能
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(Exception e){
            //明显的拒绝，其实如果开启了手动动签收模式，没有签收的情况下，会重新回到MQ
//            System.out.println("message:"+message);
//            Map<String, Object> headers = message.getMessageProperties().getHeaders();
//            System.out.println("headers:"+headers);
//            Map<String,Object> deaths = (Map<String, Object>) headers.get("x-death");
//
//            Map<String, Object> headers = message.getMessageProperties().getHeaders();
//            if(headers.containsKey("x-death")){
//                List<Map<String, Object>> deaths = (List<Map<String, Object>>) headers.get("x-death");
//                List<Map<String, Object>> collect = deaths.stream().map(map -> {
//
//                    return map;
//                }).collect(Collectors.toList());
//
//                headers.get("x-death").
//
//                if(deaths.size() > 0){
//                    Long count = (Long) deaths.get(0).get("count");
//                    System.out.println("count:"+count);
//                }
//            }

            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
