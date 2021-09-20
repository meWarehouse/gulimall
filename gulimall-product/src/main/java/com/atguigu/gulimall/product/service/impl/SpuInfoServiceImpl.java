package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeiginService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SkuHasStockVo;
import com.atguigu.gulimall.product.vo.spu.*;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    SpuImagesService spuImagesService;

    @Resource
    AttrService attrService;

    @Resource
    ProductAttrValueService productAttrValueService;

    @Resource
    SkuInfoService skuInfoService;


    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    CouponFeiginService couponFeiginService;

    @Resource
    BrandService brandService;

    @Resource
    CategoryService categoryService;

    @Resource
    WareFeignService wareFeignService;

    @Resource
    SearchFeignService searchFeignService;

    /**
     * 商品上架 将需要检索的数据存入 ElasticSearch
     *
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {



        //1.通过 spuId 在 pms_sku_info 查出 sku 信息 品牌名
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getBySpuId(spuId);

        if(skuInfoEntities == null || skuInfoEntities.size() <= 0){
            return;
        }



        //4.TODO 封装属性attrs  查询当前sku 的所有可以被检索的规格属性 相同的 spuId 的属性是一样的
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrlistforapu(spuId);
        List<SkuEsModel.Attrs> attrsList = null;
        if(productAttrValueEntities != null){
            List<Long> attrIds = productAttrValueEntities.stream().map(entry -> {
                return entry.getAttrId();
            }).collect(Collectors.toList());

            List<Long> searchAttIds = attrService.selectSearchAttIds(attrIds);

            Set<Long> attrIdsSet = new HashSet<>(searchAttIds);

            if(searchAttIds != null && searchAttIds.size() > 0){
                attrsList = productAttrValueEntities.stream().filter(item -> {
                    //过滤掉 attr_id 不在 searchAttIds 中的
                    return attrIdsSet.contains(item.getAttrId());
                }).map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item,attrs);
                    return attrs;
                }).collect(Collectors.toList());
            }
        }

        //1.TODO hasStock 是否有库存
        List<Long> skuIds = skuInfoEntities.stream().map(item -> {
            return item.getSkuId();
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try{
            R hasStock = wareFeignService.hasStock(skuIds);
            if(hasStock != null){

                TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
                };
                stockMap = hasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getId, item -> item.getHasStock()));
            }
        }catch(Exception e){ // TODO java.lang.NullPointerException: null
            log.error("库存服务异常：原因{}",e);
        }


        //封装 SkuEsModel 数据
        List<SkuEsModel.Attrs> finalAttrsList = attrsList;
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(skuInfo -> {

            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfo,skuEsModel);

            //price
            skuEsModel.setSkuPrice(skuInfo.getPrice());
            //skuImg
            skuEsModel.setSkuImg(skuInfo.getSkuDefaultImg());

            // hotScore 热度评分
            skuEsModel.setHotScore(0L);

            // 1.TODO hasStock 是否有库存
            if(finalStockMap == null){
                skuEsModel.setHasStock(true);
            }else{

                skuEsModel.setHasStock(finalStockMap.get(skuInfo.getSkuId()));
            }


            //2. 查询品牌信息封装品牌属性
            BrandEntity brandEntity = brandService.getById(skuInfo.getBrandId());
            if(brandEntity != null){
                skuEsModel.setBrandName(brandEntity.getName());
                skuEsModel.setBrandImg(brandEntity.getLogo());
            }


            //3.catagoryName
            CategoryEntity categoryEntity = categoryService.getById(skuInfo.getCatalogId());
            if(categoryEntity != null){

                skuEsModel.setCatagoryName(categoryEntity.getName());
            }


            /**
             *
             *     private List attrs;
             *     @Data
             *     public static class Attrs{
             *
             *         private Long attrId;
             *
             *         private String attrName;
             *
             *         private String attrValue;
             *     }
             */
            //4.TODO 封装属性attrs  查询当前sku 的所有可以被检索的规格属性 相同的 spuId 的属性是一样的 pms_product_attr_value
            skuEsModel.setAttrs(finalAttrsList);

            return skuEsModel;

        }).collect(Collectors.toList());

        //TODO 将数据发送给 gulimall-search 服务保存
        R r = searchFeignService.productSave(skuEsModels);

        if (r.getCode() == 0) {
            //远程调用成功
            //TODO 修改商品的状态  pms_spu_info

            //修改商品的发布状态，更新日期
            skuInfoService.updatePublishStatus(spuId, ProductConstant.ProductPublishStatusEnum.UP_STATUS.getCode());

        } else {
            //远程调用失败

            //TODO  重复调用？接口幂等性，重试机制

            /**
             * Feign调用流程
             * 1、构造请求数据，将对象转为json
             *      SynchronousMethodHandler->RequestTemplate template = this.buildTemplateFromArgs.create(argv);
             * 2、发送请求进行执行
             *      SynchronousMethodHandler->executeAndDecode(template, options);
             * 3、执行请求会有重试机制
             *      Retryer retryer = this.retryer.clone();
             *      retryer.continueOrPropagate(e);
             *      while(true) {
             *             try {
             *                 return this.executeAndDecode(template, options);
             *             } catch (RetryableException var9) {
             *                 RetryableException e = var9;
             *
             *                 try {
             *                     retryer.continueOrPropagate(e);
*/

        }




    }

    @Override
    public SpuInfoEntity spuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        SpuInfoEntity infoEntity = this.getById(byId.getSpuId());
        return infoEntity;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * //TODO 高级部分完善
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存 spu 基本信息; pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        Long spuId = spuInfoEntity.getId();


        //2、保存 spu 的描述图片  pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3、保存 spu 的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        //4、保存 spu 的规格参数 pms_sku_sale_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();

            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(byId.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;

        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);


        //5、保存 spu 的积分信息 gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R r = couponFeiginService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.info("远程保存 spu 积分信息失败");
        }


        //5、保存当前 spu 对应的所有 sku 信息
        List<Skus> skus = vo.getSkus();

        if(skus != null || skus.size() > 0){
            skus.forEach(item -> {

                //5.1)、sku 的基本信息    pms_sku_info
                String defaultImg = "";
                for (Images image : item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
//                private String skuName;
//                private BigDecimal price;
//                private String skuTitle;
//                private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //5.2)、sku 的图片信息    pms_sku_images
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());

                    return skuImagesEntity;
                }).filter(entity -> {
                    // true 保存 false 过滤
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //TODO 没有图片 路径的无需保存
                skuImagesService.saveBatch(imagesEntities);

                //5.3)、sku 的销售属性信息  pms_product_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4)、sku 的优惠满减等信息 gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeiginService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.info("远程保存 spu 优惠信息失败");
                    }

                }

            });
        }







    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {


        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        /**
         * key:
         * status: 1
         * brandId: 8
         * catelogId: 225
         */

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w -> {
                w.eq("id",key).or().like("spu_name",key);
            });
        }

        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){

            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)){
            long bId = Long.parseLong(brandId);

            if(bId > 0L){

                wrapper.eq("brand_id",brandId);
            }
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)){
            if(Long.parseLong(catelogId) > 0L){

                wrapper.eq("catalog_id",catelogId);
            }
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }



}