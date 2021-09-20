package com.atguigu.gulimall.cart.to;

import lombok.Data;
import lombok.ToString;

/**
 * @author zero
 * @create 2020-09-27 19:42
 */
@ToString
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey; //临时用户一定保存

    private Boolean tempUser = false;


}
