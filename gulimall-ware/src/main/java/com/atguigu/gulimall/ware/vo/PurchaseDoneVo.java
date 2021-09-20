package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-08-30 13:31
 */
@Data
public class PurchaseDoneVo {

    private Long id;

    private List<PurchaseItemDoneVo> items;


}
