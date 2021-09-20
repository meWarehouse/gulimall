package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.skuitem.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author zero
 * @create 2020-09-21 20:19
 */
@Slf4j
@Controller
public class ItemController {

    @Resource
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("显示："+  skuId + "的商品信息");

        SkuItemVo skuItemVo = skuInfoService.getSkuItem(skuId);
        log.info("skuItemVo:{}",skuItemVo);
        model.addAttribute("item",skuItemVo);
        return "item";
    }

}
