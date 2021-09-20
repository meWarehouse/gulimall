package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zero
 * @create 2020-08-28 23:11
 */
@Data
public class SpuBoundTo {

    private Long spuId;

    private BigDecimal buyBounds;
    private BigDecimal growBounds;


}
