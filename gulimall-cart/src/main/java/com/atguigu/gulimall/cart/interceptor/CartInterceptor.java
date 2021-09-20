package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemeberRespVo;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import jdk.internal.org.objectweb.asm.util.TraceAnnotationVisitor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 *
 * 在执行目标方法之前，判断用户的登录状态，并封装传递给controller目标请求
 *
 * @author zero
 * @create 2020-09-27 19:37
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemeberRespVo member = (MemeberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(member != null){
            //登录
            userInfoTo.setUserId(member.getId());
        }

        //没有登录
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if(CartConstant.TEMP_USER_COOKIE_NAME.equalsIgnoreCase(name)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果没有临时用户，就必须分配一个临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String s = UUID.randomUUID().toString();
            userInfoTo.setUserKey(s);
        }

        //目标方法执行之前
        threadLocal.set(userInfoTo);

        return true;
    }

    /**
     * 如果是新创建的临时用户信息，就必须在方法执行完后将设置的用户信息的cookie放入浏览器中
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();

        //如果没有临时用户就一定需要保存一个临时用户
        if(!userInfoTo.getTempUser()){
            //设置临时用户的过期时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);

        }


    }
}
