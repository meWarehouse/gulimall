package com.atguigu.gulimall.ware.vo;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author zero
 * @create 2020-08-30 13:30
 */
@Data
public class PurchaseItemDoneVo {

    //{itemId:1,status:4,reason:""}
    private Long itemId;
    private Integer status;
    private String reason;

}
