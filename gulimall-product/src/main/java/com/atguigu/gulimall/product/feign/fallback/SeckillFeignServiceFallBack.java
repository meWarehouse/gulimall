package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zero
 * @create 2020-10-16 20:50
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSeckillSkuBySkuId(Long skuId) {
        log.info("开启 getSeckillSkuBySkuId 的熔断机制...........");
        return R.error(BizCodeEnume.TOO_MACH_REQUEST.getCode(),BizCodeEnume.TOO_MACH_REQUEST.getMag());
    }
}
