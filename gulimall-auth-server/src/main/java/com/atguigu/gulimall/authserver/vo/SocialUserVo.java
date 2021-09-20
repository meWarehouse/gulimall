package com.atguigu.gulimall.authserver.vo;

import lombok.Data;

/**
 * @author zero
 * @create 2020-09-24 12:30
 */
@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
