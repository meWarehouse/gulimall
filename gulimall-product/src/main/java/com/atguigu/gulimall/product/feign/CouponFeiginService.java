package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zero
 * @create 2020-08-28 23:05
 */
@FeignClient(name = "gulimall-coupon" )
public interface CouponFeiginService {

    /***
     * 1.CouponFeiginService.saveSpuBounds(spuBoundTo);
     *      1.@RequestBody 将对象转为 json
     *      2.找到 gulimall-coupon 服务，给 /coupon/spubounds/save 发送请求
     *          将上一步的 json 放入请求体位置，发送请求
     *      3.对方服务收到请求，请求体里的 json 数据
     *          (@RequestBody SpuBoundsEntity spuBounds) 请求体的 json 转为  SpuBoundsEntity 对象
     *
     *   只要 json 数据模型是兼容的，双方服务无需使用用一个 TO
     *
     *
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/list")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
