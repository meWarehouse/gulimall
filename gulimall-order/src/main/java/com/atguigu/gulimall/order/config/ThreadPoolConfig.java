package com.atguigu.gulimall.order.config;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.config.properties.ThreadPoolConfigProperties;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author zero
 * @create 2020-10-07 12:39
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService executorService(ThreadPoolConfigProperties properties){
        return new ThreadPoolExecutor(
                properties.getCoreSize(),
                properties.getMaxSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(properties.getBlockingQueue()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

}
