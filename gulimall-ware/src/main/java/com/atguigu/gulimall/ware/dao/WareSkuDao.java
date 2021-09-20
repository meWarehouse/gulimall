package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:58:05
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId,@Param("skuNum") Integer skuNum);


    Long hasStock(Long skuId);

    List<Long> findHsstockWare(@Param("skuId") Long skuId);

    Long lockStock(@Param("skuId") Long skuId, @Param("wareid") Long wareid, @Param("num") Integer num);

    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
