package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitorderRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author zero
 * @create 2020-10-07 13:37
 */
@Slf4j
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.getConfirmInfo();
        model.addAttribute("confirmInfo",confirmVo);
        return "confirm";
    }

    /**
     * 提交订单
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes redirectAttributes){

        //下单：去创建订单，校验令牌，锁定库存...
        //下单成功来到支付选择页
        //下单失败回到订单确认页，重新确认订单
        log.info("订单提交数据{}",submitVo);

        try{
            SubmitorderRespVo respVo = orderService.submitOrder(submitVo);
            if(respVo != null && respVo.getCode() == 0){
                //处理成功 来到支付页
                model.addAttribute("submitOrderResp",respVo);

                return "pay";
            }else{
                //处理失败 失败回到订单确认页，重新确认订单
                Integer code = respVo.getCode();
                String msg = "下单失败，";
                switch (code){
                    case 1: msg+="订单已过期，请重新刷新"; break;
                    case 2: msg+="购买的商品，在购物车中有改变，请核对"; break;
                    case 3: msg+="库存不足" ; break;
                }
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch(Exception e){
            if(e instanceof NoStockException){

                redirectAttributes.addFlashAttribute("msg",e.getMessage());
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }


    }

}
