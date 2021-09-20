package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1.整合 MyBatis-Plus
 *      1).导入依赖
 *           <!-- mybatis-plus -->
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *      2）.配置
 *          1.配置数据源
 *              1）、导入数据库驱动
 *              2)、在application.yml 文件中配置数据源信息
 *          2.配置MyBatis-Plus
 *              1)、使用@MapperScan
 *              2)、告诉MyBatis-Plus sql 映射文件位置
 * 2.逻辑删除
 *      1.配置全局的逻辑删除规则
 *          mybatis-plus:
 *              global-config:
 *                  db-config:
 *                      #logic-delete-field: flag  # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
 *                      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
 *                      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
 *        2.实体类字段上加上@TableLogic注解
 *
 *
 *
 * 3.JSR303 数据校验 （校验规则：javax.validation.constraints）
 *      1）。给 Bean 添加校验注解 javax.validation.constraints，并定义自己的 message 提示
 *      2）。开启假药功能 @valid
 *          效果：校验错误后会有自定义的错误提示信息
 *      3)。给检验的bean后紧跟一个 BindingResult ，就可以获取到校验的结果
 *      4).分组校验(多场景复杂校验)
 *          1、@NotBlank(message = "品牌名不能为空",groups = {UpdateGroup.class,AddGroup.class})
 *              给校验的注解标注什么情况需要进行校验
 *          2、@Validated({AddGroup.class})
 *          3、默认没有指定分组的校验注解 @NotBlank 在分组校验的情况下@Validated({AddGroup.class})下不生效
 *      5).自定义校验
 *          1、编写一个自定义的校验注解
 *          2、编写一个自定义校验器 ConstraintValidator
 *          3。关联自定义的校验器和校验注解
 *
 * 4。统一异常处理
 * @ControllerAdvice
 *      1).编写异常处理类，使用 @ControllerAdvice
 *      2).使用 @ExceptionHandler 标注方法可以处理异常
 *
 *      --------------------------------------------------------------------------------------
 *
 *  5.模板引擎
 *      1.thymeleaf-starter 关闭缓存
 *      2.静态资源放在 static 文件夹下就可以直接按照路径进行访问
 *      3.页面放在 templates 下 直接访问
 *          springboot 访问项目，默认会找index  static.index.css
 *
 *  6.redis 缓存
 *      1.引入redis 场景启动器
 *      2.简单配置 redis 的host信息
 *      3.使用spring boot自动配置好的stringredistemplete 操作redis
 *
 *
 *  8。整合SpringCache简化缓存开发  @EnableCaching
 *      1. 引入依赖 spring-boot-starter-cache spring-boot-starter-data-redis
 *      2.写配置
 *          1).CacheAutoConfiguration会导入RedisCacheConfiguration，
 *              自动配好了缓存管理器RedisCacheManager
 *          2).配置使用Redis作为缓存
 *              spring.cache.type=redis
 *      3.测试使用缓存g
 *          @Cacheable: Triggers cache population. 触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction. 触发将数据保存到删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution. 不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method. 组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level. 在类级别共享缓存的相同配置
 *          1).开启缓存功能：@EnableCaching
 *
 *     原理：
 *      CacheManger(缓存管理器) --> Cache(缓存组件) --> Cache 负责缓存的读写操作
 *
 *
 *
 *
 *
 *
 */
@EnableRedisHttpSession
@EnableCaching
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@MapperScan("com.atguigu.gulimall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
