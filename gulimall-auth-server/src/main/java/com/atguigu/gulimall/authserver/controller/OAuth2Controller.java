package com.atguigu.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.common.vo.MemeberRespVo;
import com.atguigu.gulimall.authserver.vo.SocialUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zero
 * @create 2020-09-24 12:06
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String,String> map = new HashMap<>();

        map.put("client_id","774481333");
        map.put("client_secret","2a1200da0ad2d448cb59e2a022c4724c");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2/weibo/success");
        map.put("code",code);

        String client_id = "774481333";
        String client_secret = "2a1200da0ad2d448cb59e2a022c4724c";
        String grant_type = "authorization_code";
        String redirect_uri = "http://auth.gulimall.com/oauth2/weibo/success";



        /**
         * https://api.weibo.com/oauth2/access_token?client_id=774481333&client_secret=2a1200da0ad2d448cb59e2a022c4724c&grant_type=authorization_code&redirect_uri=http://auth.gulimall.com/oauth2/weibo/success&code=7c835f8906d23711f3bc24ed08844c36
         */
        //1.根据凑得换取accesstocken
//        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, null, map);

        CloseableHttpClient httpClient =  HttpClients.createDefault();
        String url = "https://api.weibo.com/oauth2/access_token?client_id="+client_id+"&client_secret="+client_secret+"&grant_type="+grant_type+"&redirect_uri="+redirect_uri+"&code="+code;
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse execute = httpClient.execute(httpPost);


        if(execute.getStatusLine().getStatusCode() == 200){
            //获取到里accesstocken
            String s = EntityUtils.toString(execute.getEntity());
            SocialUserVo socialUserVo = JSON.parseObject(s, SocialUserVo.class);

            //知道是哪个社交用户
            //1.当前社交用户第一次进入网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员用户）
            //登录或注册这个用户
            R r = memberFeignService.oauthLogin(socialUserVo);
            if(r.getCode() == 0){
                //登录或注册成功
                MemeberRespVo data = r.getData("data", new TypeReference<MemeberRespVo>() {});

                log.info("用户信息：{}",data);

                /*
                    1.第一次使用 session ：命令浏览器保存卡号，JSESSION这个cookie
                    以后浏览器访问那个网站就会带上这个cookie
                    子域名之间： gulimall.com auth.gulimall.com order.gulimall.com
                    发卡时()即使是子系统的卡也能让父域名使用
                 */
                //TODO 1.默认发的令牌 session=xxx 作用域是当前作用域(解决子域session共享问题)
                //TODO 2.使用JSON 的序列化方式将数据存储到redis 中
                session.setAttribute(AuthServerConstant.LOGIN_USER,data);

                //2.登录成功就跳回首页
                return "redirect:http://gulimall.com";

            }else{
                return "redirect:http://auth.gulimall.com/login.html";
            }

        }else{
            //获取失败跳转到登录页面
            return "redirect:http://auth.gulimall.com/login.html";
        }


    }



}
