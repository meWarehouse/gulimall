package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zero
 * @create 2020-10-07 16:45
 */
@Data
public class OrderItemVo {
    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;


    //TODO 商品重量
    private BigDecimal weigth;

}
