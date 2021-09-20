package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author zero
 * @create 2020-10-11 20:02
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/orderstatus/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
