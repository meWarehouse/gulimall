package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-17 20:00
 */
@Data
public class SearchParam {

    //keyword=小米&sort=saleCount_desc/asc&hasStock=0/1&skuPrice=400_1900&brandId=1&catalog3Id=1&attrs=1_3G:4G:5G&attr2=2骁龙845&attrs=4高清屏

    private String keyword; //页面传递过来的全文匹配关键字

    private Long catalog3Id; //三级分类id

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hostScore_asc/desc
     */
    private String sort; //排序条件

    /**
     * 过滤条件
     * hasStock(是否有货)、skuPrice区间、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500(1到500) _500(500以内) 500_(500以外)
     * brandId=1
     * attrs=2_5存:6寸
     */
    private Integer hasStock; // 0无货 1 有货

    private String skuPrice; // 价格区间

    private List<Long> brandId;

    private List<String> attrs;

    private Integer pageNum = 1; // 页码

    private String _querySearch;//原生的查询条件


}
