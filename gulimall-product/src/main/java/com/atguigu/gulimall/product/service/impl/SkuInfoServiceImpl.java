package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillSkuVo;
import com.atguigu.gulimall.product.vo.skuitem.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.skuitem.SkuItemVo;
import com.atguigu.gulimall.product.vo.skuitem.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sound.sampled.Line;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    AttrGroupService attrGroupService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Resource
    ExecutorService executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageBycondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wapper -> {
                wapper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)) {
            long cId = Long.parseLong(catelogId);
            if (cId > 0L) {
                queryWrapper.eq("catalog_id", cId);
            }
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
//            long bId = Long.parseLong(brandId);
//            if(bId > 0){
//                queryWrapper.eq("brand_id",bId);
//            }
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            if (new BigDecimal("0").compareTo(new BigDecimal(max)) == -1) {
                queryWrapper.le("price", max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getBySpuId(Long spuId) {

        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

    }

    @Override
    public void updatePublishStatus(Long spuId, int code) {

        baseMapper.updatePublishStatus(spuId, code);

    }

    @Override
    public SkuItemVo getSkuItem(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.sku 的基本信息获取  pms_sku_info
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            //2.sku 的图片信息 pms_sku_images
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntities);
        }, executor);

        CompletableFuture<Void> skuSaleFuture = infoFuture.thenAcceptAsync(info -> {
            //3.获取 spu 的销售属性组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> despFuture = infoFuture.thenAcceptAsync(info -> {
            //4.获取 spu 的介绍  pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(info.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> spuItemAttrGroupFuture = infoFuture.thenAcceptAsync(info -> {
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVos);
        }, executor);

        //查询但当前商品参与秒杀的详情
        CompletableFuture<Void> seckillTask = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSeckillSkuBySkuId(skuId);
            if (r.getCode() == 0) {
                SeckillSkuVo data = r.getData(new TypeReference<SeckillSkuVo>() {
                });
                System.out.println("----data:"+data);
                skuItemVo.setSeckillSkuVo(data);
            }

        }, executor);


        CompletableFuture.allOf(spuItemAttrGroupFuture,despFuture,skuSaleFuture,imgFuture,seckillTask).get();


        return skuItemVo;
    }


    public SkuItemVo getSkuItem1(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        //1.sku 的基本信息获取  pms_sku_info
        SkuInfoEntity skuInfoEntity = this.getById(skuId);
        skuItemVo.setInfo(skuInfoEntity);

        Long spuId = skuInfoEntity.getSpuId();
        Long catalogId = skuInfoEntity.getCatalogId();

        //2.sku 的图片信息 pms_sku_images
        List<SkuImagesEntity> skuImagesEntities = skuImagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(skuImagesEntities);

        //3.获取 spu 的销售属性组合
        List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(skuItemSaleAttrVos);


        //4.获取 spu 的介绍  pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
        skuItemVo.setDesp(spuInfoDescEntity);

        //5.获取 spu 的规格参数信息
        /**
         * private String groupName;
         * private List<Attr> attrs; == >
         *          private Long attrId;
         *          private String attrName;
         *          private String attrValue;
         */
        List<SpuItemAttrGroupVo> spuItemAttrGroupVos =  attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        skuItemVo.setGroupAttrs(spuItemAttrGroupVos);

        return skuItemVo;
    }


}