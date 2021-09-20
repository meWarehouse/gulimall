package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:41:54
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    Long getDefaultLevelId();
}
