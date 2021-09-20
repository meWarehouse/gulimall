package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zero
 * @create 2020-09-27 22:02
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    R skuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/saleattrlist/{skuId}")
    List<String> getSaleAttrList(@PathVariable("skuId") Long skuId);

    @PostMapping("/product/skuinfo//skuprice")
    R getskuPrice(@RequestBody List<Long> skuids);

    @GetMapping("/product/skuinfo/skuprice1")
    BigDecimal getskuPrice1(@RequestParam("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuid}/price")
    BigDecimal getPrice(@PathVariable("skuid") Long skuid);
}
