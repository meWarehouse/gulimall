package com.atguigu.gulimall.authserver.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.vo.LoginVo;
import com.atguigu.gulimall.authserver.vo.SocialUserVo;
import com.atguigu.gulimall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zero
 * @create 2020-09-23 18:16
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/registmember")
    R registMemeber(@RequestBody UserRegistVo memberVo);

    @PostMapping("/member/member/memeberlogin")
    R memeberLogin(@RequestBody LoginVo loginVo);

    @PostMapping("/member/member/oauthlogin")
    R oauthLogin(@RequestBody SocialUserVo socialUserVo) throws Exception;




    }
