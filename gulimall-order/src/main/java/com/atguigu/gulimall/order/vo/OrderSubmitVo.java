package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zero
 * @create 2020-10-08 20:40
 */
@Data
public class OrderSubmitVo {

    /*
        下订单单号无需提交需要购买的商品，去购物车在获取一次
        用户信息直接到session中取出登录用户
     */

    //用户地址id
    private Long addrId;

    //付款类型
    private Integer payType;

    //应付总额  验价
    private BigDecimal payprice;

    //防重令牌
    private String orderToken;

    //订单备注
    private String nodes;

    //TODO 发票,优惠...

}
