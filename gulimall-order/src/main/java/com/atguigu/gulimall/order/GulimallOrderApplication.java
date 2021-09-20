package com.atguigu.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 使用RabbbitMQ
 * 1.引入amqp 场景 RabbitAutoConfiguration 就会自动生效
 * 2.给容器中自动配置了
 *      RabbitMessagingTemplate RabbitTemplate AmqpAdmin RabbitConnectionFactoryCreator
 *      所有属性都在
 *          @ConfigurationProperties(prefix = "spring.rabbitmq")
 *          public class RabbitProperties {
 * 3.在配置文件中配置 spring.rabbitmq 的信息
 * 4.@EnableRabbit
 * 5.监听消息
 *      @RabbitListener 类+方法
 *      @RabbitHandler 方法
 *
 *
 *
 * 能进入订单服务的必须是登录状态下
 *
 *
 *springboot 事务的坑
 * 本地事务失效
 *      同一个对象内事务方法互调默认失效
 *       原因：绕过了代理对象，事务使用代理对象来控制的
 *      解决：使用代理对象来调用事务方法
 *      1. 引入spring-boot-starter-aop,引入了aspectj
 *      2. @EnableAspectJAutoProxy,开启aspectj动态代理功能（默认使用jdk自动生成的按照接口的代理），对外暴露代理对象，exposeProxy = true
 *      3. 本类互调用调用对象
 *
 *
 *  分布式事务 seata   http://seata.io/zh-cn/docs/user/quickstart.html
 *      1.每个微服务都必须导入  undo_log 表
 *      2.安装事务协调器 seata-server
 *      3.整合
 *          1）.导入依赖 spring-cloud-starter-alibaba-seata
 *           <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
 *             <exclusions>
 *                 <exclusion>
 *                     <artifactId>seata-all</artifactId>
 *                     <groupId>io.seata</groupId>
 *                 </exclusion>
 *             </exclusions>
 *         </dependency>
 *         //修改 io.seata 为自己 seata-server 服务器对应的版本
 *         <dependency>
 *             <groupId>io.seata</groupId>
 *             <artifactId>seata-all</artifactId>
 *             <version>0.9.0</version>
 *         </dependency>
 *        2).配置seata-server
 *          register.conf 注册中心
 *          file.config 配置中心
 *        3）。配置自己的seata DataSourceProxy 代理数据源
 *        4）每个微服务都必须导入
 *          register.conf
 *          file.conf
 *          并且：file.conf 的 service.vgroup_mapping 配置必须和spring.application.name一致
 *
 *
 *
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
@EnableRedisHttpSession
@EnableFeignClients
@EnableRabbit
@MapperScan("com.atguigu.gulimall.order.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
