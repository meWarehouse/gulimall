package com.atguigu.common.to.es;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * "properties" : {
 *         "attrs" : {
 *           "type" : "nested",
 *           "properties" : {
 *             "attrId" : {
 *               "type" : "long"
 *             },
 *             "attrName" : {
 *               "type" : "keyword"
 *             },
 *             "attrValue" : {
 *               "type" : "keyword"
 *             }
 *           }
 *         },
 *         "brandId" : {
 *           "type" : "long"
 *         },
 *         "brandImg" : {
 *           "type" : "keyword"
 *         },
 *         "brandName" : {
 *           "type" : "keyword"
 *         },
 *         "catagoryName" : {
 *           "type" : "keyword"
 *         },
 *         "catalogId" : {
 *           "type" : "long"
 *         },
 *         "hasStock" : {
 *           "type" : "boolean"
 *         },
 *         "hotScore" : {
 *           "type" : "long"
 *         },
 *         "saleCount" : {
 *           "type" : "long"
 *         },
 *         "skuId" : {
 *           "type" : "long"
 *         },
 *         "skuImg" : {
 *           "type" : "keyword"
 *         },
 *         "skuPrice" : {
 *           "type" : "keyword"
 *         },
 *         "skuTitle" : {
 *           "type" : "text",
 *           "analyzer" : "ik_smart"
 *         },
 *         "spuId" : {
 *           "type" : "keyword"
 *         }
 *       }
 *     }
 */

/**
 * @author zero
 * @create 2020-09-09 21:35
 */
@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catagoryName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs{

        private Long attrId;

        private String attrName;

        private String attrValue;
    }







}
