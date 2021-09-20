package com.atguigu.gulimall.member.interpector;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemeberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author zero
 * @create 2020-10-12 21:56
 */
@Component
public class MemeberInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemeberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();


        boolean memberMatch = antPathMatcher.match("/member/**",uri);
//        boolean alipayMatch = antPathMatcher.match("/alipay/notify", uri);

        if(memberMatch){
            return true;
        }

        HttpSession session = request.getSession();
        MemeberRespVo memeber = (MemeberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memeber != null){
            threadLocal.set(memeber);
            System.out.println("进入会员服务的用户："+memeber);
            return true;
        }else{

            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }


    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
}
