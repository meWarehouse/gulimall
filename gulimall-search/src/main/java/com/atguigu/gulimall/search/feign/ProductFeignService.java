package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-19 21:24
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    //@RequiresPermissions("product:attr:info") product/attr
    R info(@PathVariable("attrId") Long attrId);


    @GetMapping("/product/brand/infos")
    R getBrands(@RequestParam("brandIds") List<Long> brandIds);


}
