package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zero
 * @create 2020-08-30 14:21
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {


    /**
     * 信息
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    R info(@PathVariable("skuId") Long skuId);


}
