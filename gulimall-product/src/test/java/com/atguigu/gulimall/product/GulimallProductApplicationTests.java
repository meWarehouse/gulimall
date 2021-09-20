package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.atguigu.gulimall.product.vo.skuitem.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.skuitem.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Resource
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    public void testskuSaleAttrValueService(){
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(15L);
        System.out.println("返回的结果："+saleAttrsBySpuId);
    }

    @Test
    public void testattrGroupService(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(17L, 225L);
        System.out.println("返回的结果："+attrGroupWithAttrsBySpuId);
    }

    @Test
    public void testRedisson(){
        System.out.println("开始连接");
        System.out.println("redisson 连接："+redissonClient);
    }

    @Test
    public void testRedis(){

        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        //保存
        ops.set("hello","world_"+ UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");
        System.out.println("查询到的数据："+hello);


    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);

        log.info("完整路径{}",Arrays.asList(catelogPath));

    }

    @Test
    public void contextLoads() {

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");

//        brandEntity.setName("华为");
//        brandService.save(brandEntity);

//        brandService.updateById(brandEntity);
//        System.out.println("保存成功...");

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));

        list.forEach((item) -> {
            System.out.println(item);
        } );

    }

}
