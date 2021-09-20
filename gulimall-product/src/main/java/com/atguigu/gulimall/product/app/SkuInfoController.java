package com.atguigu.gulimall.product.app;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * sku信息
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 20:17:54
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuid}/price")
    public BigDecimal getPrice(@PathVariable("skuid") Long skuid){
        SkuInfoEntity byId = skuInfoService.getById(skuid);
        return byId.getPrice();
    }

    @GetMapping("/product/skuinfo/skuprice1")
    public BigDecimal getskuPrice1(@RequestParam("skuId") Long skuId){
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        return byId.getPrice();
    }

    /**
     * 通过skuid 查询价格
     */
//    @PostMapping("/product/skuinfo/skuprice")
//    Map<Long, BigDecimal> getskuPrice1(@RequestBody List<Long> skuids){
//        return null;
//    }
    @PostMapping("/skuprice")
    public R getskuPrice(@RequestBody List<Long> skuids){
        Map<Long, BigDecimal> collect = skuids.stream().collect(Collectors.toMap(
                skuid -> skuid,
                skuid -> skuInfoService.getById(skuid).getPrice()));
        return R.ok().setData(collect);
    }

    /**
     * 列表  /product/skuinfo/list
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageBycondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info") product/skuinfo
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存 /product/spuinfo/save SpuInfoEntity spuInfo
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfoEntity){
		skuInfoService.save(skuInfoEntity);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
