package com.atguigu.gulimall.member.config;

import com.atguigu.gulimall.member.interpector.MemeberInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zero
 * @create 2020-10-12 22:12
 */
@Configuration
public class MemberMvcConfig implements WebMvcConfigurer {

    @Autowired
    MemeberInterceptor memeberInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memeberInterceptor).addPathPatterns("/**");
    }
}
