package com.atguigu.gulimall.cart.config.configproperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zero
 * @create 2020-09-27 22:46
 */
@Data
@ConfigurationProperties(prefix = "mythread.pool")
@Component
public class ThreadPoolConfigProperties {

    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveTime;

    private Integer queueCapacity;

}
