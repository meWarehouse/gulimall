package com.atguigu.gulimall.cart.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zero
 * @create 2020-09-27 23:03
 */
@Configuration
public class OpenfeignConfig {

    @Bean
    Logger.Level level(){
        return Logger.Level.FULL;
    }

}
