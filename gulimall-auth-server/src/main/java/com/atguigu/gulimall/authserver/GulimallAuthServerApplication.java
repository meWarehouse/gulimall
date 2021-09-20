package com.atguigu.gulimall.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * springSession 原理
 *  1.@EnableRedisHttpSession 导入  RedisHttpSessionConfiguration 配置
 *      1.1.给容器中添加了一个组件
 *      RedisIndexedSessionRepository
 *         SessionRepository ==》 RedisIndexedSessionRepository ：redis操作session，session的增删改查封装类
 *      1.2.SessionRepositoryFilter ==》 servlet原生的Filter session存储过滤器，每个请求过来都必须经过filter
 *         1.2.1：创建该过滤器是会自动从容器中获取得到 SessionRepository
 *      ···1.2.2：原始的 request response 都被包装 SessionRepositoryRequestWrapper SessionRepositoryResponseWrapper
 *         1.2.3：以后获取session request.getSession()
 *         1.2.4：wrappedRequest.getSeeeion() ==> SessionRepository 中获取
 *
 *         @Override
 *          protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
 *          throws ServletException, IOException {
 *              request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);
 *
 *              //包装原始的请求对象
 *              SessionRepositoryRequestWrapper wrappedRequest = new SessionRepositoryRequestWrapper(request, response);
 *              //包装原始的响应对象
 *              SessionRepositoryResponseWrapper wrappedResponse = new SessionRepositoryResponseWrapper(wrappedRequest,response);
 *
 *               try {
 *                  //包装后的对象应用到后面的整个执行链
 *                  filterChain.doFilter(wrappedRequest, wrappedResponse);
 *               }
 *               finally {
 *                   wrappedRequest.commitSession();
 *              }
 *          }
 *
 *
 *
 */
@EnableRedisHttpSession //整合redis作为session存储
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}


