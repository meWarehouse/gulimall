package com.atguigu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zero
 * @create 2020-10-12 23:55
 */
@Configuration
public class OrderFeignConfig {


    @Bean
     public RequestInterceptor requestInterceptor(){
         return new RequestInterceptor() {
             @Override
             public void apply(RequestTemplate requestTemplate) {
                 ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                 if(attributes != null){
                     HttpServletRequest request = attributes.getRequest();
                     String cookie = request.getHeader("Cookie");
                     requestTemplate.header("Cookie",cookie);
                 }
             }
         };
     }

}
