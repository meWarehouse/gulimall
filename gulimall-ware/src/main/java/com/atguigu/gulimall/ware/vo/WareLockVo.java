package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-10-09 11:12
 */
@Data
public class WareLockVo {
    //单号
    private String orderSn;

    private List<LockInfo> infoList;

    @Data
    public static class LockInfo{
        //数量
        private Integer num;
        //锁定的商品
        private Long skuId;
    }

}
