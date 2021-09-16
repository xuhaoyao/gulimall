package com.scnu.gulimall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username, @RequestParam("password") String password,
                          @RequestParam(value = "url",required = false) String url,
                          HttpSession session,
                          HttpServletResponse response){
        if(StringUtils.hasLength(username) && StringUtils.hasLength(password)){
            //登录成功
            String uuid = UUID.randomUUID().toString();
            response.addCookie(new Cookie("token",uuid));
            session.setAttribute("user",username);
            redisTemplate.opsForValue().set(uuid,username);
            if(StringUtils.hasLength(url)) {
                return "redirect:" + url + "?token=" + uuid;
            }
            else{
                return "hello";
            }
        }
        return "login";
    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam(value = "redirect_url",required = false) String url,
                            @CookieValue(value = "token",required = false) String token,
                            Model model){
        if(StringUtils.hasLength(token)){
            return "redirect:" + url + "?token=" + token;
        }
        model.addAttribute("url",url);
        return "login";
    }

}
