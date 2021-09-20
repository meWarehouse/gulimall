package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.skuitem.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 17:48:33
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);


    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageBycondition(Map<String, Object> params);

    List<SkuInfoEntity> getBySpuId(Long spuId);

    void updatePublishStatus(Long spuId, int code);

    SkuItemVo getSkuItem(Long skuId) throws ExecutionException, InterruptedException;
}

