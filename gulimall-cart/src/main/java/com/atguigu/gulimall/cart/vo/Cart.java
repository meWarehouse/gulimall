package com.atguigu.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zero
 * @create 2020-09-27 16:21
 */
public class Cart {

    private List<CartItem> items;
    //商品数量
    private Integer countNum;
    //商品类型数量
    private Integer countType;
    //商品总价
    private BigDecimal totalAmount;
    //减免
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    //商品数量
    public Integer getCountNum() {
        Integer count = 0;
        if(items != null && items.size() > 0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }

        return count;
    }

    //商品类型数量
    public Integer getCountType() {
        Integer count = 0;
       if(items != null && items.size() > 0){
           for (CartItem item : items) {
               count+=1;
           }
       }
        return count;
    }


    //商品总价
    public BigDecimal getTotalAmount() {

        BigDecimal amount = new BigDecimal("0");
        if(items != null && items.size() > 0){
            for (CartItem item : items) {
                if(item.getCkeck()){

                    amount = amount.add(item.getTotalPrice());
                }
            }
        }

        //减去优惠总价
        amount = amount.subtract(getReduce());

        return amount;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
