package com.atguigu.gulimall.search.vo;

import lombok.Data;

/**
 * @author zero
 * @create 2020-09-19 21:33
 */
@Data
public class AttrRespVo {

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

    private Long attrId;
    private String attrName;
    private Integer searchType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;

    private Long attrGroupId;

    private Integer valueType;

}
