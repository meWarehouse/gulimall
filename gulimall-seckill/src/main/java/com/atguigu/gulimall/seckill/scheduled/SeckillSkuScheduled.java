package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author zero
 * @create 2020-10-13 22:22
 *
 *
 *  秒杀商品定时上架
 *      每天凌晨3点上架最近3天的商品
 *      当天 00:00:00 - 23:59:59
 *      明天 00:00:00 - 23:59:59
 *      后天 00:00:00 - 23:59:59
 *
 *
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    public static final String UPLOAD_LOCK= "seckill:upload:lock";

    //TODO 幂等性处理
    @Scheduled(cron = "* * * * * *")
    public void uploadSeckillSkuLatest3Days(){

        RLock lock = redissonClient.getLock(UPLOAD_LOCK);

        lock.lock();
        try {
            log.info("上架秒杀商品信息...................");
            //不需要处理重复上架问题
            seckillService.uploadSeckillSkuLatest3Days();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }


    }




}
