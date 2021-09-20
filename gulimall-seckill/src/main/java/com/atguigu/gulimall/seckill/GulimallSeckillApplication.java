package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 *
 * 整合sentinel
 *  1。导入依赖
 *      spring-cloud-starter-alibaba-sentinel
 *  2.下载对应的控制台
 *  3.配置sentinel的控制台地址信息
 *  4.在控制台调整参数【默认重启失效】
 *
 *  每个微服务导入 actuator
 *  自定义sentinel流控返回数据
 *
 *  使用sentinel保护feign远程调用：熔断
 *     1.openfeign 版本 ！！！ 2.2.0.RELEASE
 *     2.调用方的熔断保护 feign.sentinel.enabled=true
 *     3.调用方手动指定降级策略.远程服务被降级处理，触发熔断回调方法
 *     4.超大流量时，必须牺牲一些远程服务，在服务的提供方(远程服务)指定降级策略
 *          提供方在运行，但是不运行自己的业务逻辑，返回的时默认的熔断数据(限流数据)
 *
 *
 *   自定义降级
 *      try (Entry entry = SphU.entry("resourceName")) {
 *           // 被保护的业务逻辑
 *          // do something here...
 *      } catch (BlockException ex) {
 *          // 资源访问阻止，被限流或被降级
 *          // 在此处进行相应的处理操作
 *       }
 *      2.注解
 *           @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandlerSeckullSkus")
 *
 *     无论是何种方式都需要配置被限流后的默认返回
 *     url 可以统一返回 WebCallbackManager
 *
 *
 *.🕊
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
