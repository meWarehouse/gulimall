package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:31:45
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
