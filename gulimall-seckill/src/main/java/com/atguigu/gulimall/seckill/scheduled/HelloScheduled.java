package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author zero
 * @create 2020-10-13 20:55
 */
@Slf4j
@Component
//@EnableScheduling //开启定时任务
//@EnableAsync
public class HelloScheduled {


    /**
     * 秒 分 时 日 月 周
     *
     * 1. spring中6位组成，不允许第7位的年
     * 2. 在周几的位置，1-7代表周一到周日 MON-SUN
     * 3. 定时任务不应该阻塞。默认是阻塞的
     *      解决不阻塞：
     *              1. 可以让业务运行以异步的方式，自己提交到线程池
     *              2. 支持定时任务线程池，设置
     *                  自动配置：TaskSchedulingAutoConfiguration
     *                  TaskSchedulingProperties    spring.task.scheduling.pool.size=5
     *              3. 让定时任务异步执行
     *                  自动配置：TaskExecutionAutoConfiguration
     *                  @EnableAsync @Async
     *
     */
//    @Async
//    @Scheduled(cron = "* * * * * *")
    public void hello(){
        log.info("hello.....");
        try{ TimeUnit.SECONDS.sleep(3); }catch( InterruptedException e ){ e.printStackTrace(); }
    }

}
