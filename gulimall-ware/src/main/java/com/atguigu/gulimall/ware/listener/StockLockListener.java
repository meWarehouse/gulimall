package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author zero
 * @create 2020-10-11 20:53
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue" )
public class StockLockListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(Message message, StockLockedTo to, Channel channel) throws IOException {

        System.out.println("收到库存信息....");
        try{
            wareSkuService.releaseStockLock(to);
            //执行成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(Exception e){
            //执行失败
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }


    }

    /**
     *
     * 解锁取消的订单
     * @param message
     * @param to
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(Message message, OrderTo to, Channel channel) throws IOException {

        System.out.println("取消订单解锁....");
        try{
            wareSkuService.releaseStockLock(to);
            //执行成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(Exception e){
            //执行失败
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }


    }

}
