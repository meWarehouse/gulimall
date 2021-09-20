package com.atguigu.gulimall.product.vo.skuitem;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.vo.SeckillSkuVo;
import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-21 21:12
 */
@Data
public class SkuItemVo {

    //1.sku 的基本信息获取  pms_sku_info
    private SkuInfoEntity info;

    //2.sku 的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;

    //有无货
    private Boolean hasStock = true;

    //3.获取 spu 的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4.获取 spu 的介绍  pms_spu_info_desc
    private SpuInfoDescEntity desp;

    //5.获取 spu 的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //秒杀信息
    private SeckillSkuVo seckillSkuVo;

}
