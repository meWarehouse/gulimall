package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 17:48:33
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();


    void removeMenuByIds(List<Long> asList);

    /**
     * 找到 category 的完整路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> searchLevel1();

    Map<String, List<Catalog2Vo>> getCatalogJson();

}

