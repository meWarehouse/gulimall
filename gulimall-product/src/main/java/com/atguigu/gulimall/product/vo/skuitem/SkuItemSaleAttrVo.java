package com.atguigu.gulimall.product.vo.skuitem;

import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-21 21:18
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValuesWithSkuId> valuesWithSkuIds;
}
