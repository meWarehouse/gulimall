package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-11 8:41
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2Vo {

    /**
     *  "catalog1Id":"1",
     *             "catalog3List":Array[4],
     *             "id":"1",
     *             "name":
     */
    private String catalog1Id; //一级分类id
    private List<Catalog3Vo> catalog3List; //三级分类集合
    private String id; //二级分类id
    private String name; //二级分类名


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo{
        /**
         *
         "catalog2Id":"1",
         "id":"1",
         "name":"电子书"
         */
        private String catalog2Id; //二级分类id
        private String id; //三级分类id
        private String name; //三级分类名
    }





}
