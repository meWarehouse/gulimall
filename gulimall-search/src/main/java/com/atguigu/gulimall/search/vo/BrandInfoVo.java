package com.atguigu.gulimall.search.vo;


import lombok.Data;


/**
 * @author zero
 * @create 2020-09-20 15:27
 */
@Data
public class BrandInfoVo {

    private Long brandId;
    private String name;
    private String logo;
    private String descript;
    private Integer showStatus;
    private String firstLetter;
    private Integer sort;


}
