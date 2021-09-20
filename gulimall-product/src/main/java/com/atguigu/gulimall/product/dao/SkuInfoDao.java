package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * sku信息
 * 
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 17:48:33
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {


    void updatePublishStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
