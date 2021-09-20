package com.atguigu.gulimall.authserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zero
 * @create 2020-09-22 20:26
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        /**
         * @GetMapping("/login.html")
         *     public String LoginPage(){
         *         return "login";
         *     }
         */
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
