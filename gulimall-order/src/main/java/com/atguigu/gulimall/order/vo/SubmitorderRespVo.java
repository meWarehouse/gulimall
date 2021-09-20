package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author zero
 * @create 2020-10-08 21:17
 */
@Data
public class SubmitorderRespVo {

    //状态码 0-成功  其他失败
    private Integer code = 0;

    private OrderEntity order;

}
