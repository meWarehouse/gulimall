package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zero
 * @create 2020-09-17 20:06
 */
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 一下是分页信息
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPage;
    private List<Integer> pageNavs;

    private List<BrandVo> brands; //当前查询到的结果，所有涉及到的品牌信息
    private List<CatalogVo> catalogs; //所有涉及的分类
    private List<AttrVo> attrs; //所有涉及的属性

    //=============== 以上是返回给页面的所有信息 ===================

    //面包屑
    private List<Navo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();


    @Data
    public static class Navo{
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }





}
