package com.atguigu.gulimall.authserver.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zero
 * @create 2020-09-22 22:35
 */
@FeignClient("gulimall-third-party")
public interface ThirdpartyService {

    @PostMapping("/sms/sendsms")
    R sendSms(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
