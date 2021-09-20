package com.atguigu.gulimall.order.vo;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zero
 * @create 2020-10-07 16:42
 */

public class OrderConfirmVo {

    //订单防重令牌
    @Getter @Setter
    private String orderTocken;


    //用户收货地址信息
    @Getter @Setter
    private List<MemberAddressVo> address;

    //商品信息
    @Getter @Setter
    List<OrderItemVo> items;

    //有货无货
    @Getter @Setter
    private Map<Long,Boolean> hasStock;

    //发票....

    //优惠信息
    @Getter @Setter
    private Integer integration;

    public Integer getProductNum(){
        Integer sum = 0;
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                sum+=item.getCount();
            }
        }
        return sum;
    }

    //订单总额
    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0.00");
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    //应付价格
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {

        return this.getTotal();
    }





}
