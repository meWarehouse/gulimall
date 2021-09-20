package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author zero
 * @create 2020-10-11 16:56
 */
@Data
public class StockLockedTo {

    private Long taskId; //库存工作单id
    private StockDetailTo detail; //工作单详情id

}
