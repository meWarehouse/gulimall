package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * @author zero
 * @create 2020-09-25 18:56
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录即可访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String helloPage(){
        return "hello";
    }

    /**
     * 需要登录才能访问
     *
     *  该方法需要能够感知是否在 ssoserver 登录页面登录过
     *
     * @param model
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model  model, HttpSession session,@RequestParam(value = "tocken",required = false) String tocken){

        //如果 有 tocken 就可以认为是已登录过的
        if(!StringUtils.isEmpty(tocken)){
            //TODO 去ssoserver登录页面获取tocken 的真正信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userinfo?tocken=" + tocken, String.class);

            session.setAttribute("loginUser",forEntity.getBody());
        }

        Object loginUser = session.getAttribute("loginUser");
        if(loginUser != null){

            //有过登录 跳转到list页面
            List<String> emps = Arrays.asList("小小","大大","老王");
            model.addAttribute("emps",emps);

            return "list";
        }else{
            //没有登录 跳转到登录页面

            //跳转过去后，使用url上的查询参数标识当前的页面地址
            return "redirect:"+ssoServerUrl+"?redirt_url=http://client1.com:8001/employees";
        }

    }



}
