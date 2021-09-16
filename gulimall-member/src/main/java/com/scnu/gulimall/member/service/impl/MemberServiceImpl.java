package com.scnu.gulimall.member.service.impl;

import com.scnu.gulimall.member.dao.MemberLevelDao;
import com.scnu.gulimall.member.entity.MemberLevelEntity;
import com.scnu.gulimall.member.exception.EmailException;
import com.scnu.gulimall.member.exception.UserNameException;
import com.scnu.gulimall.member.service.MemberLevelService;
import com.scnu.gulimall.member.to.GiteeTo;
import com.scnu.gulimall.member.to.UserLoginTo;
import com.scnu.gulimall.member.to.UserRegisterTo;
import com.scnu.gulimall.member.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scnu.common.utils.PageUtils;
import com.scnu.common.utils.Query;

import com.scnu.gulimall.member.dao.MemberDao;
import com.scnu.gulimall.member.entity.MemberEntity;
import com.scnu.gulimall.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterTo to) throws EmailException,UserNameException {
        //判断邮箱是否重复
        Integer emailCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("email", to.getEmail()));
        if(emailCount != 0){
            throw new EmailException("该邮箱已被注册!");
        }
        //判断用户名是否重复
        Integer usernameCount = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", to.getUserName()));
        if(usernameCount != 0){
            throw new UserNameException("用户名已存在!");
        }

        //查出用户的默认会员等级->TODO 此处后台在插入会员等级表的时候,还需要判断是否已有默认等级 即default_status只能有一条记录是1
        MemberLevelEntity memberLevelEntity = memberLevelDao.selectOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));

        MemberEntity member = new MemberEntity();
        member.setLevelId(memberLevelEntity.getId());
        member.setUsername(to.getUserName());
        member.setPassword(new BCryptPasswordEncoder().encode(to.getPassword()));
        member.setEmail(to.getEmail());
        member.setNickname(to.getUserName());
        baseMapper.insert(member);
    }

    @Override
    public UserInfoVo login(UserLoginTo to) throws LoginException {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<MemberEntity>()
                .eq("username", to.getAccount())
                .or()
                .eq("email", to.getAccount())
                .or()
                .eq("mobile", to.getAccount());
        MemberEntity memberEntity = baseMapper.selectOne(wrapper);
        if(memberEntity == null){
            throw new LoginException("账号不存在!");
        }
        boolean matches = new BCryptPasswordEncoder().matches(to.getPassword(), memberEntity.getPassword());
        if(!matches){
            throw new LoginException("密码错误!");
        }
        return new UserInfoVo().setNickName(memberEntity.getNickname());
    }

    @Override
    public UserInfoVo giteeLogin(GiteeTo to) {
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("gitee_id", to.getGiteeId()));
        if(memberEntity == null){
            memberEntity = new MemberEntity();
            memberEntity.setGiteeId(to.getGiteeId());
            memberEntity.setGiteeName(to.getGiteeName());
            memberEntity.setNickname(to.getGiteeName());
            baseMapper.insert(memberEntity);
        }else{
            //信息有变化,更新
            if(!to.getGiteeName().equals(memberEntity.getGiteeName())){
                memberEntity.setGiteeName(to.getGiteeName());
                baseMapper.updateById(memberEntity);
            }
        }
        return new UserInfoVo().setNickName(to.getGiteeName());
    }

}