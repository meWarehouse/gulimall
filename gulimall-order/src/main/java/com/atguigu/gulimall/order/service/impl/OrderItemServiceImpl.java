package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * queues：声明需要监听的所有队列
     *
     * class org.springframework.amqp.core.Message
     *
     * 参数可以写以下类型
     * 1、Message message：原生消息详细详细。头+体
     * 2、T<发送的消息的类型> OrderReturnReasonEntity content;
     * 3、Channel channel：当前传输数据的通道
     *
     * Queue：可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一个收到此消息
     *
     * 1、订单服务启动多个：同一个消息，只能有一个客户端能收到
     * 2、只有一个消息完全处理完，方法运行结束，我们就可以接收下一个消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity entity, Channel channel){
//        System.out.println("接收到消息 ...内容"+entity);
//        MessageProperties messageProperties = message.getMessageProperties();
//        System.out.println("messageProperties:"+messageProperties+"==>Channel:"+channel);

        System.out.println("接收到消息 ...内容"+entity);
//        try{ TimeUnit.SECONDS.sleep(3); }catch( InterruptedException e ){ e.printStackTrace(); }

        System.out.println("消息处理完成："+entity.getName());

        // deliveryTag 是 channel 内自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag:"+deliveryTag);

        // basicNack(long deliveryTag, boolean multiple, boolean requeue)
        try {
            if(deliveryTag % 2 ==0){
                //签收
                channel.basicAck(deliveryTag,false);
                System.out.println(deliveryTag+"--:被签收");
            }else{
                //拒收
                //long deliveryTag, boolean multiple, boolean requeue【true重新入队 入队后会继续投递 false没有签收直接丢弃】
                channel.basicNack(deliveryTag,false,false);
                //long deliveryTag, boolean requeue
//                channel.basicReject();
                System.out.println(deliveryTag+"--:没被签收");
            }
        } catch (IOException e) {
            //网络异常
            e.printStackTrace();
        }

    }

    @RabbitHandler
    public void receiveMessage(OrderItemEntity entity){
//        System.out.println("接收到消息 ...内容"+entity);
//        MessageProperties messageProperties = message.getMessageProperties();
//        System.out.println("messageProperties:"+messageProperties+"==>Channel:"+channel);

        System.out.println("接收到消息 ...内容"+entity);
//        try{ TimeUnit.SECONDS.sleep(3); }catch( InterruptedException e ){ e.printStackTrace(); }
        System.out.println("消息处理完成："+entity.getOrderSn());


    }




}