package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author zero
 * @create 2020-09-25 19:41
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/userinfo")
    public String userInfo(@RequestParam("tocken") String tocken){
        String s = redisTemplate.opsForValue().get(tocken);
        return s;
    }

    /**
     * 跳转到登录页面
     * @return
     */
    @GetMapping("/login.html")
    public String login(@RequestParam("redirt_url") String redirectUrl, Model model,
                        @CookieValue(value = "sso_tocken",required = false) String tocken){

        if(!StringUtils.isEmpty(tocken)){
            //说明以前有人登录过 并留下了痕迹
            return "redirect:"+redirectUrl+"?tocken="+tocken;
        }

        model.addAttribute("url",redirectUrl);

        return "login";
    }

    @PostMapping("/dologin")
    public String dologin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url, HttpServletResponse response){

        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){
            //登录成功跳转回原来的页面

            //跳转为原来的页面前先将 用户信息进行保存
            String replace = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(replace,username);

            response.addCookie(new Cookie("sso_tocken",replace));

            //为了能够使其他页面能够感知到，以登录状态 在路径上加一个tocken参数
            return "redirect:"+url+"?tocken="+replace;
        }

        //登录失败 返回登录页面
        return "login";
    }


}
