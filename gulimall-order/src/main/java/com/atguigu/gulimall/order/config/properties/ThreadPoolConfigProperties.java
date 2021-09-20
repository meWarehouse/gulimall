package com.atguigu.gulimall.order.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zero
 * @create 2020-10-07 12:40
 */
@ConfigurationProperties(prefix = "mythread.pool")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
    private Integer blockingQueue;

}
