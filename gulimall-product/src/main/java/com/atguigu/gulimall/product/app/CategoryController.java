package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.R;


/**
 * 商品三级分类
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 20:17:53
 */
@RestController
@Slf4j
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping("/list")
    public R allList() {
        List<CategoryEntity> categoryEntityList = categoryService.list();

        //所有一级菜单集合
        List<CategoryEntity> leaveOneMenus = categoryEntityList.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() * 1 == 0;
        }).collect(Collectors.toList());

        //为一级设置子菜单
        List<CategoryEntity> leaveOneMenusWithChildern = leaveOneMenus.stream().map(categoryEntity -> {
            categoryEntity.setChildren(getChilderns(categoryEntity, categoryEntityList, leaveOneMenus));
            return categoryEntity;
        }).collect(Collectors.toList());


        return R.ok().put("allData", leaveOneMenusWithChildern);
    }

    private List<CategoryEntity> getChilderns(CategoryEntity root, List<CategoryEntity> all, List<CategoryEntity> removeMenus) {

        all.removeAll(removeMenus);

        List<CategoryEntity> entities = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() * 1 == root.getCatId() * 1;
        }).collect(Collectors.toList());

        List<CategoryEntity> entitiesWithChildern = entities.stream().map(categoryEntity -> {
            categoryEntity.setChildren(getChilderns(categoryEntity, all, entities));
            return categoryEntity;
        }).collect(Collectors.toList());


        return entitiesWithChildern;
    }


    /**
     * 查询所有分类以及子分类，以树形结构组装
     */
    @RequestMapping("/list/tree")
    public R list() {

        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category) {
//		categoryService.updateById(category);
        categoryService.updateCascade(category);
        return R.ok();
    }

    @RequestMapping("/update/sort")
    public R bachUpdate(@RequestBody CategoryEntity[] category) {
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 删除
     *
     * @RequestBody:获取请求体，必须发送post请求 SpringMvc自动将请求的数据(json), 转为对应的对象
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds) {

        //检查当前菜单是否被别的地方引用
        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
