package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-09 23:44
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     *
     * 1.R 设计的时候可以加上泛型 R<T>
     * 2.直接返回想要的结果 List<SkuHasStockVo>
     * 3.自己封装解析结构
     *
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);

}
