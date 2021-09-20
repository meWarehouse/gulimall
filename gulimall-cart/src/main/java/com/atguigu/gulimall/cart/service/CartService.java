package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zero
 * @create 2020-09-27 17:23
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车信息
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     * @param key
     */
    void clearCart(String key);

    /**
     * 修改购物项状态(选中/不选中)
     * @param skuId
     * @param check
     */
    void changeItemCheck(Long skuId, Integer check);

    /**
     * 改变购物车中的购物项数量
     * @param skuId
     * @param num
     */
    void itemNum(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获取当前用户所有选中的购物项
     * @param skuId
     * @return
     */
    List<CartItem> CurrentUserCartitems();
}
