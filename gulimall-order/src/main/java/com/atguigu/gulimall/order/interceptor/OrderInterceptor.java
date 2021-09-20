package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemeberRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author zero
 * @create 2020-10-07 13:40
 */
@Slf4j
@Component
public class OrderInterceptor implements HandlerInterceptor {

    public static  ThreadLocal<MemeberRespVo> threadLocal = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        //放行解锁远程调用
        //   /order/order/orderstatus/{orderSn}
        String requestURI = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/orderstatus/**", requestURI);
        boolean alipayMatch = antPathMatcher.match("/alipay/notify", requestURI);

        if(match || alipayMatch){
            return true;
        }


        HttpSession session = request.getSession();
        MemeberRespVo member = (MemeberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(member != null){
            //有登录 将用户保存
            threadLocal.set(member);
            log.info("进入order服务的用户：{}",member);
            return true;
        }else{
            //没有登录 重定向到登录页面
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }
}
