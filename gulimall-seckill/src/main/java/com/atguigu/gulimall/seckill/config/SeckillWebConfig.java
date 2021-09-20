package com.atguigu.gulimall.seckill.config;

import com.atguigu.gulimall.seckill.interceptor.GulimallInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zero
 * @create 2020-10-15 22:40
 */
@Configuration
public class SeckillWebConfig implements WebMvcConfigurer {

    @Autowired
    GulimallInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
