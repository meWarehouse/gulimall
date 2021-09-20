package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.RegistMemberVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:41:54
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void registMemeber(RegistMemberVo memberVo);

    //检查用户名及电话
    void checkPhoneUnique(String phone) throws PhoneExistException;
    void checkUsernameUnique(String userName) throws UsernameExistException;


    MemberEntity memeberLogin(MemberLoginVo loginVo);

    MemberEntity oauthLogin(SocialUserVo socialUserVo) throws Exception;
}

