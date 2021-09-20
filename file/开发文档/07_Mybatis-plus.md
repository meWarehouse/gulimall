---
typora-root-url: images
---

# 约定>配置>编码



```java
规定数据库中的数据删减使用 show_status 字段维护 是否显示[0-不显示，1显示]
```

## 配置Mybatis-plus 逻辑删除

[逻辑删除](https://baomidou.com/guide/logic-delete.html)

```xml
1.
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: flag  # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)

2.实体类字段上加上@TableLogic注解
	@TableLogic(value = "1")
	private Integer showStatus;
```

##配置Mybatis-plus分页插件版本问题

![Snipaste_2020-09-04_16-25-17](Snipaste_2020-09-04_16-25-17.jpg)

[分页插件版本问题](https://baomidou.com/guide/interceptor.html)

```java
从 Mybatis Plus 3.4开始将使用新的分页配置，原来的分页配置类 PaginationInterceptor 已经被弃用
更多配置参考 Mybatis Plus 官方文档: https://baomidou.com/guide/interceptor.html
3.4 前 PaginationInterceptor
@Configuration
@MapperScan("com.baomidou.cloud.service.*.mapper*")
public class MybatisPlusConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
        return paginationInterceptor;
    }
}

3.4 后 MybatisPlusInterceptor
@Configuration
@MapperScan("scan.your.mapper.package")
public class MybatisPlusConfig {

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }
}

```

