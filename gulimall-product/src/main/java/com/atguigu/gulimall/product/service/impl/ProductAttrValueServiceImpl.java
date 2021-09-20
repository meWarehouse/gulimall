package com.atguigu.gulimall.product.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.ProductAttrValueDao;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        if(collect == null || collect.size() ==0 ){

        }else{
            this.saveBatch(collect);
        }
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrlistforapu(Long spuId) {

        List<ProductAttrValueEntity> valueEntities = this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));


        return valueEntities;
    }

    @Override
    public void updateBySupId(Long spuId, List<ProductAttrValueEntity> productAttrValueEntity) {

        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        List<ProductAttrValueEntity> collect = productAttrValueEntity.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();

            BeanUtils.copyProperties(item,valueEntity);
            valueEntity.setSpuId(spuId);
            return valueEntity;
        }).collect(Collectors.toList());

        this.saveBatch(collect);



    }

}