package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zero
 * @create 2020-09-15 19:49
 */
@Configuration
public class MyRedissonConfig {

    @Bean
    public RedissonClient redissonClient(){

        Config config = new Config();
        //redis:// or rediss://
        config.useSingleServer().setAddress("redis://192.168.44.104:6379");
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }

}
