package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author zero
 * @create 2020-10-06 21:42
 */
@Slf4j
@Controller
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/send")
    public String receiveMessage(){

        for (int i = 1; i <= 10; i++) {
            if(i%2==0){

                OrderReturnReasonEntity order = new OrderReturnReasonEntity();
                order.setId(1L);
                order.setName("hello+"+i);
                order.setSort(1);
                order.setStatus(0);
                order.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchage","hello.java",order,new CorrelationData(UUID.randomUUID().toString()));
//            log.info("消息发送成功:{}",order.getName());
            }else{
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchage","hello.java22",orderItemEntity,new CorrelationData(UUID.randomUUID().toString()));
            }
        }

        return "ok";
    }

}
