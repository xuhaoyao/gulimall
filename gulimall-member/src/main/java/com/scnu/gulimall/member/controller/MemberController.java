package com.scnu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.scnu.gulimall.member.exception.EmailException;
import com.scnu.gulimall.member.exception.UserNameException;
import com.scnu.gulimall.member.feign.CouponFeignService;
import com.scnu.gulimall.member.to.GiteeTo;
import com.scnu.gulimall.member.to.UserLoginTo;
import com.scnu.gulimall.member.to.UserRegisterTo;
import com.scnu.gulimall.member.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.scnu.gulimall.member.entity.MemberEntity;
import com.scnu.gulimall.member.service.MemberService;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.R;

import javax.security.auth.login.LoginException;


/**
 * 会员
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:14:11
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/gitee/login")
    public R giteeLogin(@RequestBody GiteeTo to){
        UserInfoVo userInfoVo = memberService.giteeLogin(to);
        return R.ok().put("data",userInfoVo);
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginTo to){

        try {
            UserInfoVo vo = memberService.login(to);
            return R.ok().put("data",vo);
        } catch (LoginException e) {
            return R.error().put("msg",e.getMessage());
        }

    }

    @PostMapping("/register")
    public R register(@RequestBody UserRegisterTo to){
        try {
            memberService.register(to);
        } catch (EmailException e) {
            return R.error().put("msg",e.getMessage());
        } catch (UserNameException e) {
            return R.error().put("msg",e.getMessage());
        }
        return R.ok();
    }

    /**
     * 测试远程调用
     */
    @GetMapping("/userCoupons")
    public R userCoupons(){
        return R.ok().put("user",new MemberEntity()).put("coupons",couponFeignService.userCoupons());
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
