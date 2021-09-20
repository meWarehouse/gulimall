package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.MyThreadPoolConfig;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author zero
 * @create 2020-09-27 17:24
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ExecutorService executors;

    private final String CART_PREFIX = "gulimall:cart:";


    /**
     * 将商品信息保存到redis中
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> operations = getOperatorCart();


        String res = (String) operations.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            //redis 中没有该商品
            //添加新商品
            //封装购物项信息
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                //调用远程服务查询sku详细信息
                R r = productFeignService.skuInfo(skuId);
                if (r.getCode() == 0) {
                    SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    cartItem.setSkuId(skuId);
                    cartItem.setCkeck(true);
                    cartItem.setCount(num);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                }
            }, executors);

            CompletableFuture<Void> getSkuAttrListFuture = CompletableFuture.runAsync(() -> {
                //远程查询对应的销售属性信息
                List<String> saleAttrList = productFeignService.getSaleAttrList(skuId);
                cartItem.setSkuAttr(saleAttrList);
            }, executors);

            CompletableFuture.allOf(getSkuInfoFuture,getSkuAttrListFuture).get();
            //将数据存入redis
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        }
        else{
            //有改商品 则只需要更新数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        BoundHashOperations<String, Object, Object> operatorCart = getOperatorCart();
        String s  = (String) operatorCart.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null){
            //登录
            //判断临时购物车中有没有数据，有则合并到用户购物车，然后删除临时购物车中的数据
            //用户购物车
            String userKey = CART_PREFIX+userInfoTo.getUserId();

            //临时购物车
            String tempUserKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempUserKey);
            if(tempCartItems != null && tempCartItems.size() > 0){
                //将临时购物车中的数据合并到用户购物车
                for (CartItem cartItem : tempCartItems) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //合并完后将临时购物车中的数据删除
                clearCart(tempUserKey);
            }

            //获取登录后的购物车的数据【包含合并过来的临时购物车的数据，和登录后的购物车的数据】
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);

        }else{
            //没登陆
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            cart.setItems(cartItems);
        }

        return cart;
    }

    @Override
    public void clearCart(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void changeItemCheck(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> operatorCart = getOperatorCart();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCkeck(check==1?true:false);
        operatorCart.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public void itemNum(Long skuId, Integer num) {

        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> operatorCart = getOperatorCart();
        operatorCart.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operatorCart = getOperatorCart();
        operatorCart.delete(skuId.toString());
    }

    @Override
    public List<CartItem> CurrentUserCartitems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() == null){
            return null;
        }


        List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());


        List<Long> collect1 = cartItems.stream().map(CartItem::getSkuId).collect(Collectors.toList());

        R r = productFeignService.getskuPrice(collect1);
        Map<Long, BigDecimal> priceMap = r.getData(new TypeReference<Map<Long, BigDecimal>>() {
        });

        List<CartItem> collect = cartItems.stream()
                .filter(cartItem -> cartItem.getCkeck())
                .map(cartItem -> {
            //更新当前sku 的价格
//            cartItem.setPrice(priceMap.get(cartItem.getSkuId()));
//            BigDecimal bigDecimal = productFeignService.getskuPrice1(cartItem.getSkuId());
//                    BigDecimal price = productFeignService.getPrice(cartItem.getSkuId());
                    cartItem.setPrice(priceMap.get(cartItem.getSkuId()));
            return cartItem;
        }).collect(Collectors.toList());

        return collect;
    }

    private List<CartItem> getCartItems(String userKey) {
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(userKey);
        List<Object> values = operations.values();
        List<CartItem> cartItems = null;
        if(values != null && values.size() > 0){
            cartItems = values.stream().map(obj -> JSON.parseObject((String) obj, CartItem.class)).collect(Collectors.toList());
        }
        return cartItems;
    }


    /**
     * 获取操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getOperatorCart() {
        //获取用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";

        //判断用户类型
        if (userInfoTo.getUserId() != null) {
            //登录用户   gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //临时用户  gulimall:cart:xxxx
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        return operations;
    }
}
