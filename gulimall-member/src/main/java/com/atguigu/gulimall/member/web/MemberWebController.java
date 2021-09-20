package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zero
 * @create 2020-10-12 21:45
 */
@Slf4j
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/orderlist.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, Model model){

        Map<String,Object> map = new HashMap<>();
        map.put("page",pageNum);
//        map.put("limit",10);
        //查出当前会员的所有订单
        R r = orderFeignService.listWithItem(map);

        log.info("当前登录用户的所用订单：{}", r);
        model.addAttribute("orders",r);

        return "orderList";
    }

}
