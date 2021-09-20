package com.scnu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.scnu.common.constant.auth.AuthConstant;
import com.scnu.common.utils.R;
import com.scnu.common.vo.UserInfoVo;
import com.scnu.gulimall.auth.feign.MemberFeignService;
import com.scnu.gulimall.auth.to.GiteeTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/oauth2/gitee")
public class GiteeController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @Value("${oauth.gitee.client-id}")
    private String clientId;

    @Value("${oauth.gitee.client-secret}")
    private String clientSecret;

    @GetMapping("/success")
    public String success(String code, HttpSession session){
        //System.out.println("code:" + code);
        //https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}
        //根据code获取token
        Map<String,Object> map = new HashMap<>();
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("client_id",clientId);
        map.put("redirect_uri","http://auth.gulimall.com/oauth2/gitee/success");
        map.put("client_secret",clientSecret);
        String s = restTemplate.postForObject("https://gitee.com/oauth/token", map, String.class);
        JSONObject jsonObject = JSON.parseObject(s);
        String accessToken = jsonObject.getString("access_token");
        if(StringUtils.hasLength(accessToken)) {
            /**
             *  TODO
             *  此处可以往数据库中添加gitee用户的一些信息
             *  对于不同的社交登录,分别创建表与之对应
             *  gitee对应gitee表,微信对应微信表
             *  分别的,就应该创建GiteeController,WechatController
             *  用户还可以进行关联操作,再用gitee或者微信登录成功之后,必要的步骤就是将社交登录账号,与平台账号进行关联
             *  例如京东或者B站,再社交登录成功之后,不会立即跳转到首页,而是要与平台绑定账号,如果没有在平台注册过账号的话,必须先注册,再绑定,才能登录
             *  这么做的好处估计就是让多个社交账号能同时登录->等价于平台账号的登录
             *  在做数据处理的时候更方便些
             *  因此就需要创建关联表
             *  微信表,gitee表,平台账号表,关联表
             *  关联表类似如下
             *  id wechat_id gitee_id,member_id
             *
             *  总结流程如下:
             *  新用户用微信登录->微信登录成功->跳转到绑定页面->输入平台信息进行绑定->未绑定则必须先注册->绑定成功进入主页
             */
            //拿着token得到用户资料
            String userInfo = restTemplate.getForObject("https://gitee.com/api/v5/user?access_token=" + accessToken, String.class);
            JSONObject userJson = JSONObject.parseObject(userInfo);
            GiteeTo to = new GiteeTo();
            to.setGiteeId(userJson.getString("id"));
            to.setGiteeName(userJson.getString("name"));
            //System.out.println(userInfo);
            R r = memberFeignService.giteeLogin(to);
            UserInfoVo data = r.getData("data", new TypeReference<UserInfoVo>(){});
            session.setAttribute(AuthConstant.SESSION_USER_NAME,data);
            return "redirect:http://gulimall.com";
        }
        else{
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }

}
