package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.RegistMemberVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;

import javax.annotation.Resource;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    MemberDao memberDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void registMemeber(RegistMemberVo vo) {

        //注册会员
        MemberEntity memberEntity = new MemberEntity();

        //刚注册的会员设置为默认的等级
        memberEntity.setLevelId(memberDao.getDefaultLevelId());

        //创建时间
//        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        memberEntity.setCreateTime(new Date());

        /**
         * 异常机制
         * 检查用户名及手机号是否唯一，为了能让controller感知异常，使用异常机制
         */
        this.checkPhoneUnique(vo.getPhone());
        this.checkUsernameUnique(vo.getUserName());

        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setNickname(vo.getUserName());


        //密码存入数据库需要进行加密处理

        memberEntity.setPassword(new BCryptPasswordEncoder().encode(vo.getPassword()));

        //保存注册信息
        baseMapper.insert(memberEntity);


    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {

        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(mobile > 0){
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkUsernameUnique(String userName) throws UsernameExistException {
        Integer username = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(username > 0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity memeberLogin(MemberLoginVo loginVo) {

        String loginacct = loginVo.getLoginacct();
        String password = loginVo.getPassword();

        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        if(entity != null){
            //进行密码验证
            if(new BCryptPasswordEncoder().matches(password, entity.getPassword())){
                return entity;
            }else{
                return null;
            }
        }else{
            //没有改用户 登录失败

            return null;
        }

    }

    /**
     * 登录注册二合一
     * @param socialUserVo
     * @return
     */
    @Override
    public MemberEntity oauthLogin(SocialUserVo socialUserVo) throws Exception {

        String uid = socialUserVo.getUid();
        //判断数据库中是否有改用户的uid
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(entity != null){
            //以前有登录 更信息 进行登录
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(entity.getId());
            memberEntity.setAccessToken(socialUserVo.getAccess_token());
            memberEntity.setExpiresIn(socialUserVo.getExpires_in());

            baseMapper.updateById(memberEntity);

            entity.setAccessToken(socialUserVo.getAccess_token());
            entity.setExpiresIn(socialUserVo.getExpires_in());

            return entity;

        }else{
            //以前没有登录 获取社交信息 进行注册

            //获取信息
            Map<String,String> query = new HashMap<>();
            query.put("access_token",socialUserVo.getAccess_token());
            query.put("uid",socialUserVo.getUid());

            MemberEntity memberEntity = new MemberEntity();


            HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
            //获取信息出现异常不处理
            try{
                if(response.getStatusLine().getStatusCode() == 200){
                    //获取数据成功 进行用户登录
                    JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(response.getEntity()));


                    memberEntity.setNickname(jsonObject.getString("name"));
                    memberEntity.setGender("m".equalsIgnoreCase(jsonObject.getString("gender"))?1:0);



                }
            }catch(Exception e){}

            memberEntity.setSocialUid(socialUserVo.getUid());
            memberEntity.setAccessToken(socialUserVo.getAccess_token());
            memberEntity.setExpiresIn(socialUserVo.getExpires_in());

            //插入数据库
            baseMapper.insert(memberEntity);

            return memberEntity;
        }




    }

}