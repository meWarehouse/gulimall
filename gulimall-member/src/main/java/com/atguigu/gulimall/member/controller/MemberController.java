package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.member.exception.PhoneExistException;
import com.atguigu.gulimall.member.exception.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.RegistMemberVo;
import com.atguigu.gulimall.member.vo.SocialUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.annotation.Resource;


/**
 * 会员
 *
 * @author zero
 * @email zero@gmail.com
 * @date 2020-07-14 22:41:54
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Resource
    CouponFeignService couponFeignService;

    /**
     * 社交用户登录或注册
     * @param socialUserVo
     * @return
     */
    @PostMapping("/oauthlogin")
    public R oauthLogin(@RequestBody SocialUserVo socialUserVo) throws Exception {

        MemberEntity entity = memberService.oauthLogin(socialUserVo);

        if(entity != null){
            //登录成功
            return R.ok().setData(entity);
        }else{
            //登录失败
            return R.error(BizCodeEnume.ACCOUT_PASSEORD_EXCEPTION.getCode(),BizCodeEnume.ACCOUT_PASSEORD_EXCEPTION.getMag());
        }


    }

    /**
     * 用户登录
     * @param loginVo
     * @return
     */
    @PostMapping("/memeberlogin")
    public R memeberLogin(@RequestBody MemberLoginVo loginVo){

        MemberEntity entity = memberService.memeberLogin(loginVo);

        if(entity != null){
            //登录成功
            return R.ok().setData(entity);
        }else{
            //登录失败
            return R.error(BizCodeEnume.ACCOUT_PASSEORD_EXCEPTION.getCode(),BizCodeEnume.ACCOUT_PASSEORD_EXCEPTION.getMag());
        }


    }

    /**
     * 用户注册
     * @param memberVo
     * @return
     */
    @PostMapping("/registmember")
    public R registMemeber(@RequestBody RegistMemberVo memberVo){

        try{
            memberService.registMemeber(memberVo);
        }catch(PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMag());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(),BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMag());
        }finally {

        }
        return R.ok();
    }

    @RequestMapping("/test")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");


        R membercoupons = couponFeignService.membercoupons();

        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));

    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
