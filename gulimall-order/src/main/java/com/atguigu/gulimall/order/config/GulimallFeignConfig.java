package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zero
 * @create 2020-10-07 19:57
 */
@Configuration
public class GulimallFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        RequestInterceptor requestInterceptor= new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // RequestContextHolder 拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                System.out.println("requestInterceptor 线程："+Thread.currentThread().getId());
                if(attributes != null){
                    //老请求
                    HttpServletRequest request = attributes.getRequest();
                    //同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    //设置新请求
                    requestTemplate.header("Cookie",cookie);
                }


            }
        };
/*
        RequestInterceptor requestInterceptor = (RequestTemplate requestTemplate) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String cookie = request.getHeader("Cookie");
            requestTemplate.header("Cookie",cookie);
        };*/

        return requestInterceptor;
    }

}
