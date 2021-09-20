package com.atguigu.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemeberRespVo;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.gulimall.authserver.feign.ThirdpartyService;
import com.atguigu.gulimall.authserver.vo.LoginVo;
import com.atguigu.gulimall.authserver.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zero
 * @create 2020-09-22 19:27
 */
@Controller
public class LoginController {


    /*@GetMapping("/login.html")
    public String LoginPage(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage(){
        return "reg";
    }
*/

    @Resource
    ThirdpartyService thirdpartyService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/login/sms")
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 接口防刷

        /**
         * 在60s内同一个手机号重复提交 及验证码有效期 验证码校验
         *
         *  将验证码存入redis中并设置有效期 phone:code
         *  当接收到需要发验证码请求时，先查看redis是否有指定手机号的键
         *  没有则发送有则提示
         *
         */
        String cacheCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(cacheCode)) {
            if ((System.currentTimeMillis() - Long.parseLong(cacheCode.split("_")[1])) < 60000) {
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMag());
            }

        }

        String substring = UUID.randomUUID().toString().substring(0, 5);
        String code = substring + "_" + System.currentTimeMillis();
        //不能保证第三方服务一定能将验证码发送出去，所以应该先存入redis
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        thirdpartyService.sendSms(phone, substring);

        return R.ok();
    }


    /**
     * TODO 重定向携带数据(RedirectAttributes) 利用session原理，将数据放入session中
     * 只要跳到下一个页面，取出这个数据以后，session中的数据就会删除
     * <p>
     * TODO 1.分布式下的session问题
     *
     * @param
     * @param bindingResult
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Validated UserRegistVo vo, BindingResult bindingResult, /*Model model*/ RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest servletRequest) {
        servletRequest.getSession();
        //错误返回到注册页面
        if (bindingResult.hasErrors()) {
          /*  Map<String, String> collect = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));*/
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors", errors);
            /**
             * 转发到注册页面
             *      return "forward:/reg.html"
             *          Request method 'POST' not supported
             *          原因：
             *              用户注册--》/regist【post】 --> 转发reg.html（路径映射默认都是get方式）
             *               post方式给get方式发送请求 Request method 'POST' not supported
             *
             * 转发引起表单重复提交 --》最好的的解决方式就是重定向
             *  return "reg"; //转发
             *
             *      RedirectAttributes 解决重定向数据携带问题
             *
             */
//            return "reg"; //转发
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //真正注册调用远程服务

        //1.检查验证码
        String cacheCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(cacheCode)) {
            //redis中有对应的验证码
            if (cacheCode.split("_")[0].equals(vo.getCode())) {
                //删除redis中的验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过 //调用远程服务
                R r = memberFeignService.registMemeber(vo);
                if (r.getCode() == 0) {
                    //保存成功
                    //注册成功返回到登录页面

                    return "redirect:http://auth.gulimall.com/login.html"; //重定向
                } else {
                    //保存失败 获取返回错误信息 重回定向到注册页面
                    redirectAttributes.addFlashAttribute("errors", new HashMap<String, String>().put("msg", r.getData(new TypeReference<String>() {
                    })));

                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            } else {
                //验证码输入错误
                redirectAttributes.addFlashAttribute("errors", new HashMap<String, String>().put("code", "验证码错误"));
                return "redirect:http://auth.gulimall.com/reg.html";
            }

        } else {
            //redis 中没有对应的验证码
            redirectAttributes.addFlashAttribute("errors", new HashMap<String, String>().put("code", "验证码已过期"));
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(LoginVo vo,RedirectAttributes redirectAttributes,HttpSession session) {

        R r = memberFeignService.memeberLogin(vo);
        if(r.getCode() == 0){
            //登录成功
            //登录成功返回首页
            //将用户信息保存到session 中
            MemeberRespVo data = r.getData("data", new TypeReference<MemeberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else{
            //登陆失败
            redirectAttributes.addFlashAttribute("errors",new HashMap<String,String>().put("msg",r.getData(new TypeReference<String>(){})));
            return "redirect:http://auth.gulimall.com/login.html";

        }


    }

}
