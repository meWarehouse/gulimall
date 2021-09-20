package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zero
 * @create 2020-09-27 17:26
 */
@Slf4j
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/cart/currentusercartitem")
    public List<CartItem> getCurrentUserCartitems(){
        List<CartItem> cartItems = cartService.CurrentUserCartitems();
        return cartItems;
    }


    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/changeItemNum")
    public String itemNum(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.itemNum(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/changeItemCheck")
    public String changeItemCheck(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.changeItemCheck(skuId,check);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 浏览器有一个cookie, user-key:标识用户身份，一个月后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器以后保存，每次访问都会带上这个cookie
     * <p>
     * 登录：session有
     * 没登录：按照cookie里面带来的user-key来做
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session,Model model) throws ExecutionException, InterruptedException {



        /*
        //判断用户登录与否的全部放在拦截器中执行
        Object loginUser = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(loginUser == null){
            //没有登录获取临时购物车
        }else{
            //已登录获取用户沟渠车
        }
        */

        //快速获取到用户信息
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        log.info("用户信息:{}", userInfoTo);

        //获取购物车信息返回给页面
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
//        model.addAttribute("skuId",skuId);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 避免重复刷新提交   解决重复提交
     * 添加商品到购物车
     * RedirectAttributes
     *     ra.addFlashAttribute();将数据放在session里面可以在页面取出，但是只能取一次
     *     ra.addAttribute("skuId",skuId); 将数据放在请求路径后
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSucccessPage(@RequestParam("skuId") Long skuId,Model model){
        //重定向到成功页面，再次查询出数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }


}
