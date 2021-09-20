package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zero
 * @create 2020-09-22 22:31
 */
@RequestMapping("/sms")
@RestController
public class SmsController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供给其他服务调用而不是由前端直接调用
     * @param phone
     * @param code
     * @return
     */
    @PostMapping("/sendsms")
    public R sendSms(@RequestParam("phone") String phone, @RequestParam("code") String code){
        smsComponent.sendSmsCode(phone,code);
        return R.ok();
    }

}
