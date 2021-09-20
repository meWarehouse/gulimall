package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemeberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.GulimallInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import com.mysql.cj.Session;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringSummary;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginContext;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zero
 * @create 2020-10-13 22:51
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    public static final String SESSION_CACHE_PREDIX = "seckill:session:";
    public static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + 商品随机码


    /**
     * 上架最近3天的秒杀商品
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描最近3天才与的秒杀活动
        R r = couponFeignService.getLately3DaysSeckillSku();

        if(r.getCode() == 0){
            //上架商品
            List<SeckillSessionVo> data = r.getData(new TypeReference<List<SeckillSessionVo>>() {
            });
            if(data != null && data.size() > 0){

                //缓存到redis
                //1.缓存活动信息
                saveSeeeionInfo(data);
                //2.缓存活动关联的商品信息
                saveSessionSkuInfo(data);
            }
        }
    }

    public List<SeckillSkuRedisTo> blockHandlerSeckullSkus(BlockException e) {
        log.error("原方法 getCurrentSeckillSkus 被降级处理././././././");
        return null;

    }

    /**
     * blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常
     * @return
     */
//    @SentinelResource(value = "getCurrentSeckillSkusResource",blockHandler = "blockHandlerSeckullSkus")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> skuRedisTos = null;
        try (Entry entry = SphU.entry("seckillSkus")) {

            long time = System.currentTimeMillis();
            //keys seckill:session:*
            Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREDIX +   "*");
            if (keys == null || keys.size() == 0) {
                return null;
            }

            for (String key : keys) {
                String replace = key.replace(SESSION_CACHE_PREDIX, "");
                String[] s = replace.split("_");

                log.info("now:{}", time);
                String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.parseLong(s[0]));
                String format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.parseLong(s[1]));
                log.info("start:{} ==> {} ", Long.parseLong(s[0]), format);
                log.info("end:{} ==> {}", Long.parseLong(s[1]), format1);

                if (time >= Long.parseLong(s[0]) && time <= Long.parseLong(s[1])) {
                    //获取当前场次的所有秒杀商品
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    if (range != null && range.size() > 0) {
                        //绑定 hsah
                        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                        List<String> multiGets = hashOps.multiGet(range);
                        if (multiGets != null && multiGets.size() > 0) {
                            skuRedisTos = multiGets.stream().map(item -> {
                                //TODO 如果是预告信息一定到屏蔽随机码
                                SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                                return skuRedisTo;
                            }).collect(Collectors.toList());
                        }

                    }

                }

            }


        } catch (Exception e) {
            log.info("seckillSkus 被降级处理..............");
        } finally {

        }

        return skuRedisTos;

    }

    /**
     * 获取指定的 skuId 是否有秒杀活动，有返回详情
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo SeckillSkuBySkuId(Long skuId) {

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //获取当前hash的所有key
        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0){
            String regx = "\\d-"+skuId;
            for (String key : keys) {
                if(Pattern.matches(regx,key)){
                    String s = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(s, new TypeReference<SeckillSkuRedisTo>() {
                    });


                    long time = new Date().getTime();
                    // 屏蔽随机码    判断该sku 当前是否处于秒杀状态
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();

                    //预告或正处于秒杀
                    if(time<startTime || (time>=startTime && time<endTime)){

                        if(time >= startTime && time <= endTime){

                        }else{
                            skuRedisTo.setRandomCode(null);
                        }

                        return skuRedisTo;
                    }


                }
            }
        }


        return null;
    }

    /**
     * 秒杀处理
     * @param killId
     * @param key
     * @param num
     */
    @Override
    public String kill(String killId, String key, Integer num) {

        long l = System.currentTimeMillis();

        MemeberRespVo member = GulimallInterceptor.threadLocal.get();

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if(!StringUtils.isEmpty(s)){ //有数据
            //数据校验

            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);

            //1.秒杀时间
            long now = System.currentTimeMillis();
            if(now >= skuRedisTo.getStartTime() && now <= skuRedisTo.getEndTime()){
                //2.随机码
                String skuid = skuRedisTo.getPromotionSessionId()+"-"+skuRedisTo.getSkuId();
                if(skuRedisTo.getRandomCode().equals(key) && skuid.equals(killId)){
                    //3.购买数量是否合理
                    if(num<=skuRedisTo.getSeckillLimit().intValue()){
                        //4.幂等性 判断该用户是否已经买过 userId-场次-skuId
                        String buyKey = member.getId()+"-"+skuid;
                        Long ttl = skuRedisTo.getEndTime() - now;
                        Boolean isBuy = redisTemplate.opsForValue().setIfAbsent(buyKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if(isBuy){
                           //占位成功没有买过

                           //抢占信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE+skuRedisTo.getRandomCode());
                            //semaphore.acquire(); 阻塞式等待
//                            try {
//                                boolean b = semaphore.tryAcquire(num, 20, TimeUnit.MILLISECONDS);
//                                if(b){
//
//                                }r
//                            } catch (InterruptedException e) {
//
//                            }
                            boolean b = semaphore.tryAcquire(num);
                            if(b){
                                //给MQ 发送消息
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(member.getId());
                                orderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
                                orderTo.setSkuId(skuRedisTo.getSkuId());
                                orderTo.setNum(num);
                                orderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                System.out.println("秒杀时间:"+(System.currentTimeMillis()- l));
                                return timeId;
                            }
                        }
                    }
                }
            }
        }

        return null;

    }


    /**
     * key=开始时间_结束时间
     * value=当前场次-skuId
     *
     * @param data
     */
    private void saveSeeeionInfo(List<SeckillSessionVo> data){
        //缓存活动信息
        if(data != null){
            data.stream().forEach(session->{
                long start = session.getStartTime().getTime();
                long end = session.getEndTime().getTime();
                //判断当前 key 是否存在
                String key =SESSION_CACHE_PREDIX+ start+"_"+end;
                Boolean b = redisTemplate.hasKey(key);
                if(!b){ //不存在时才保存 存在就无需任何操作
                    log.info("此处只准进入一次......");
                    // 场次-skuid
                    List<String> collect = session.getRelations().stream().map(s -> s.getPromotionSessionId()+"-"+s.getSkuId().toString()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key,collect);
                }
            });
        }


    }

    /**
     * 哈希
     * Map<String,Map<String,Object>>
     *
     *     seckill:skus,<当前场次-skuId,sku的秒杀信息及详情信息>
     *
     * @param data
     */
    private void saveSessionSkuInfo(List<SeckillSessionVo> data){
        if(data != null){
            return;
        }
        //2.缓存活动关联的商品信息 秒杀信息+商品的基本信息
        data.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelations().stream().forEach(seckillSku->{
               SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo();
                Boolean b = hashOps.hasKey(seckillSku.getPromotionSessionId()+"-"+seckillSku.getSkuId());
                if(!b){
                    log.info("此处也只能进入一次.......");
                    //保存sku基本信息
                    R r = productFeignService.getSkuInfo(seckillSku.getSkuId());
                    if(r.getCode() ==0){
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        skuRedisTo.setSkuInfo(skuInfo);
                    }
                    //保存秒杀信息
                    BeanUtils.copyProperties(seckillSku,skuRedisTo);

                    //保存秒杀时间
                    skuRedisTo.setStartTime(session.getStartTime().getTime());
                    skuRedisTo.setEndTime(session.getEndTime().getTime());

                    //保存随机码
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    skuRedisTo.setRandomCode(randomCode);

                    //使用库存作为分布式的信号量  限流    seckill:stock:随机码
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE+randomCode);
                    //使用该商品的件数作为信号量
                    semaphore.trySetPermits(seckillSku.getSeckillCount().intValue());

                    String s = JSON.toJSONString(skuRedisTo);
                    hashOps.put(seckillSku.getPromotionSessionId()+"-"+seckillSku.getSkuId(),s);
                }

           });

        });


    }


}
