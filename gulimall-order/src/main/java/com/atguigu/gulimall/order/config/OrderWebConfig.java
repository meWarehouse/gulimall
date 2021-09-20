package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.OrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zero
 * @create 2020-10-07 13:42
 */
@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    @Autowired
    OrderInterceptor orderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).addPathPatterns("/**");
    }
}
