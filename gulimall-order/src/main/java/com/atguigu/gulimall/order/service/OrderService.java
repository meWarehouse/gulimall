package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:50:10
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo getConfirmInfo() throws ExecutionException, InterruptedException;

    SubmitorderRespVo submitOrder(OrderSubmitVo submitVo);

    Integer orderStatusByOrdersn(String orderSn);


    void closeOrder(OrderEntity orderEntity);

    /**
     * 支付宝
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils listWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo payAsyncVo);

    void handleSeckillOrder(SeckillOrderTo orderTo);
}

