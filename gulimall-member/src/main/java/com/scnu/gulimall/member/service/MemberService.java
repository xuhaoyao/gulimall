package com.scnu.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.member.entity.MemberEntity;
import com.scnu.gulimall.member.exception.EmailException;
import com.scnu.gulimall.member.exception.UserNameException;
import com.scnu.gulimall.member.to.UserLoginTo;
import com.scnu.gulimall.member.to.UserRegisterTo;

import javax.security.auth.login.LoginException;
import java.util.Map;

/**
 * 会员
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:14:11
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 用户注册
     * @param to
     * @throws EmailException  邮箱已存在
     * @throws UserNameException 用户名已存在
     */
    void register(UserRegisterTo to) throws EmailException, UserNameException;

    /**
     * 用户登录
     * @param to
     * @throws LoginException
     */
    void login(UserLoginTo to) throws LoginException;
}

