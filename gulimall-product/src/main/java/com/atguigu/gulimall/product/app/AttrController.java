package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 20:17:54
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Resource
    ProductAttrValueService productAttrValueService;




    ///product/attr/base/listforspu/{spuId}
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforapu(@PathVariable("spuId") Long spuId){

       List<ProductAttrValueEntity> valueEntities =  productAttrValueService.baseAttrlistforapu(spuId);

        return R.ok().put("data",valueEntities);
    }


    ///product/attr/info/{attrId}

    //product/attr/sale/list/{catelogId}
    //product/attr/base/list/{catelogId}
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType){

        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);


        return R.ok().put("page",page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    //    ///product/attr/info/{attrId}
    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info") product/attr
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);

        AttrRespVo respVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
//    public R save(@RequestBody a attr){
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     *  product/attr/update
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
//    public R update(@RequestBody AttrEntity attr){
    public R update(@RequestBody AttrVo attr){
		attrService.updateById(attr);

        return R.ok();
    }

//    /product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateBySupId(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> productAttrValueEntity){

        productAttrValueService.updateBySupId(spuId,productAttrValueEntity);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
