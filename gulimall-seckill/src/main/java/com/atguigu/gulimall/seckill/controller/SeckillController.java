package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author zero
 * @create 2020-10-14 20:52
 */
@Slf4j
@RequestMapping("/seckill")
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 当前正在参与秒杀的所有商品
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> skuRedisTos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(skuRedisTos);
    }

    @ResponseBody
    @GetMapping("/seckillSku/{skuId}")
    public R getSeckillSkuBySkuId(@PathVariable("skuId") Long skuId){
        log.info("/seckillSku/{skuId}..................");

        SeckillSkuRedisTo skuRedisTo = seckillService.SeckillSkuBySkuId(skuId);

//        try { Thread.sleep(300); } catch (InterruptedException e) {e.printStackTrace();}

        return R.ok().setData(skuRedisTo);
    }

    //kill?killId="+skill+"key="+key+"num="+num;
    @GetMapping("/kill")
    public String kill(@PathParam("killId") String killId,
                       @PathParam("key") String key,
                       @PathParam("num") Integer num,
                       Model model){

        //登录校验
        log.info("killId:{},key:{},num:{}",killId,key,num);

        String ordersn = seckillService.kill(killId, key, num);
        model.addAttribute("ordersn",ordersn);

        return "success";
    }

}
