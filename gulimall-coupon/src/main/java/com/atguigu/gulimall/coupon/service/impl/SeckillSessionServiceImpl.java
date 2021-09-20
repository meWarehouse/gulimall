package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;

@Slf4j
@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> lately3DaysSeckillInfo() {

        log.info("start:{}",startTime());
        log.info("end:{}",endTime());

        List<SeckillSessionEntity> sessionEntities = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if(sessionEntities != null && sessionEntities.size() > 0){
            List<SeckillSessionEntity> collect = sessionEntities.stream().map(entity -> {
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", entity.getId()));
                entity.setRelations(relationEntities);

                return entity;
            }).collect(Collectors.toList());
            log.info("最近3天需要上架的商品{}",collect);
            return collect;
        }

        return null;
    }

    private String startTime(){
        // 2020-10-14 00:00:00
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        String start = LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }
    private String endTime(){
        // 2020-10-16 23:59:59
        LocalDate localDate = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
//        LocalDateTime.of(localDate,max).plusHours(8);
        String end = LocalDateTime.of(localDate, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return end;
    }



}