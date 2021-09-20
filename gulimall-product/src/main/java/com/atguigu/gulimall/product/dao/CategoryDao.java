package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 17:48:33
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
