package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.vo.skuitem.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {

        /**
         * #传入spuid
         * #info.sku_id ,
         * #分析当前spu有多少个sku，所有sku涉及到的属性组合
         *
         * SELECT
         * 	ssav.`attr_id`,
         * 	ssav.`attr_name`,
         * 	GROUP_CONCAT(DISTINCT ssav.`attr_value`)
         * 	FROM `pms_sku_info` info
         * LEFT JOIN `pms_sku_sale_attr_value` ssav ON ssav.`sku_id` = info.`sku_id`
         * WHERE info.`spu_id` = 15
         * GROUP BY ssav.`attr_id`,ssav.`attr_name`
         */
        return baseMapper.getSaleAttrsBySpuId(spuId);


    }

    @Override
    public List<String> getSaleAttrList(Long skuId) {

        /**
         * SELECT CONCAT(attr_name,":",attr_value)
         * FROM `pms_sku_sale_attr_value`
         * WHERE sku_id = 71
         */

        return baseMapper.getSaleAttrList(skuId);
    }

}