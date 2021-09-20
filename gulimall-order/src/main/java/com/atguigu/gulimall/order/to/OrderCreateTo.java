package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zero
 * @create 2020-10-08 23:02
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;
    private List<OrderItemEntity> orderItem;
    private BigDecimal payPrice;
    private BigDecimal fare;

}
