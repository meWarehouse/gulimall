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
    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; // + ???????????????


    /**
     * ????????????3??????????????????
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //????????????3????????????????????????
        R r = couponFeignService.getLately3DaysSeckillSku();

        if(r.getCode() == 0){
            //????????????
            List<SeckillSessionVo> data = r.getData(new TypeReference<List<SeckillSessionVo>>() {
            });
            if(data != null && data.size() > 0){

                //?????????redis
                //1.??????????????????
                saveSeeeionInfo(data);
                //2.?????????????????????????????????
                saveSessionSkuInfo(data);
            }
        }
    }

    public List<SeckillSkuRedisTo> blockHandlerSeckullSkus(BlockException e) {
        log.error("????????? getCurrentSeckillSkus ???????????????././././././");
        return null;

    }

    /**
     * blockHandler ??????????????????????????????/??????/????????????????????????????????? fallback ????????????????????????????????????
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
                    //???????????????????????????????????????
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    if (range != null && range.size() > 0) {
                        //?????? hsah
                        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                        List<String> multiGets = hashOps.multiGet(range);
                        if (multiGets != null && multiGets.size() > 0) {
                            skuRedisTos = multiGets.stream().map(item -> {
                                //TODO ?????????????????????????????????????????????
                                SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                                return skuRedisTo;
                            }).collect(Collectors.toList());
                        }

                    }

                }

            }


        } catch (Exception e) {
            log.info("seckillSkus ???????????????..............");
        } finally {

        }

        return skuRedisTos;

    }

    /**
     * ??????????????? skuId ???????????????????????????????????????
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo SeckillSkuBySkuId(Long skuId) {

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //????????????hash?????????key
        Set<String> keys = hashOps.keys();
        if(keys != null && keys.size() > 0){
            String regx = "\\d-"+skuId;
            for (String key : keys) {
                if(Pattern.matches(regx,key)){
                    String s = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(s, new TypeReference<SeckillSkuRedisTo>() {
                    });


                    long time = new Date().getTime();
                    // ???????????????    ?????????sku ??????????????????????????????
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();

                    //????????????????????????
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
     * ????????????
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
        if(!StringUtils.isEmpty(s)){ //?????????
            //????????????

            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);

            //1.????????????
            long now = System.currentTimeMillis();
            if(now >= skuRedisTo.getStartTime() && now <= skuRedisTo.getEndTime()){
                //2.?????????
                String skuid = skuRedisTo.getPromotionSessionId()+"-"+skuRedisTo.getSkuId();
                if(skuRedisTo.getRandomCode().equals(key) && skuid.equals(killId)){
                    //3.????????????????????????
                    if(num<=skuRedisTo.getSeckillLimit().intValue()){
                        //4.????????? ????????????????????????????????? userId-??????-skuId
                        String buyKey = member.getId()+"-"+skuid;
                        Long ttl = skuRedisTo.getEndTime() - now;
                        Boolean isBuy = redisTemplate.opsForValue().setIfAbsent(buyKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if(isBuy){
                           //????????????????????????

                           //???????????????
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE+skuRedisTo.getRandomCode());
                            //semaphore.acquire(); ???????????????
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
                                //???MQ ????????????
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(member.getId());
                                orderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
                                orderTo.setSkuId(skuRedisTo.getSkuId());
                                orderTo.setNum(num);
                                orderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                System.out.println("????????????:"+(System.currentTimeMillis()- l));
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
     * key=????????????_????????????
     * value=????????????-skuId
     *
     * @param data
     */
    private void saveSeeeionInfo(List<SeckillSessionVo> data){
        //??????????????????
        if(data != null){
            data.stream().forEach(session->{
                long start = session.getStartTime().getTime();
                long end = session.getEndTime().getTime();
                //???????????? key ????????????
                String key =SESSION_CACHE_PREDIX+ start+"_"+end;
                Boolean b = redisTemplate.hasKey(key);
                if(!b){ //????????????????????? ???????????????????????????
                    log.info("????????????????????????......");
                    // ??????-skuid
                    List<String> collect = session.getRelations().stream().map(s -> s.getPromotionSessionId()+"-"+s.getSkuId().toString()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key,collect);
                }
            });
        }


    }

    /**
     * ??????
     * Map<String,Map<String,Object>>
     *
     *     seckill:skus,<????????????-skuId,sku??????????????????????????????>
     *
     * @param data
     */
    private void saveSessionSkuInfo(List<SeckillSessionVo> data){
        if(data != null){
            return;
        }
        //2.????????????????????????????????? ????????????+?????????????????????
        data.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelations().stream().forEach(seckillSku->{
               SeckillSkuRedisTo skuRedisTo = new SeckillSkuRedisTo();
                Boolean b = hashOps.hasKey(seckillSku.getPromotionSessionId()+"-"+seckillSku.getSkuId());
                if(!b){
                    log.info("???????????????????????????.......");
                    //??????sku????????????
                    R r = productFeignService.getSkuInfo(seckillSku.getSkuId());
                    if(r.getCode() ==0){
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        skuRedisTo.setSkuInfo(skuInfo);
                    }
                    //??????????????????
                    BeanUtils.copyProperties(seckillSku,skuRedisTo);

                    //??????????????????
                    skuRedisTo.setStartTime(session.getStartTime().getTime());
                    skuRedisTo.setEndTime(session.getEndTime().getTime());

                    //???????????????
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    skuRedisTo.setRandomCode(randomCode);

                    //???????????????????????????????????????  ??????    seckill:stock:?????????
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE+randomCode);
                    //???????????????????????????????????????
                    semaphore.trySetPermits(seckillSku.getSeckillCount().intValue());

                    String s = JSON.toJSONString(skuRedisTo);
                    hashOps.put(seckillSku.getPromotionSessionId()+"-"+seckillSku.getSkuId(),s);
                }

           });

        });


    }


}
