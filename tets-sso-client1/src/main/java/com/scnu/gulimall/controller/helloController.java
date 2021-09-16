package com.scnu.gulimall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class helloController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/boss")
    public String boss(HttpServletRequest request){
        String token = request.getParameter("token");
        HttpSession session = request.getSession();
        if(StringUtils.hasLength(token)){
            String s = redisTemplate.opsForValue().get(token);
            session.setAttribute("user",s);
            return "list";
        }
        if(session.getAttribute("user") != null){
            return "list";
        }
        return "redirect:http://ssoserver.com:8000/login.html" + "?redirect_url=" + request.getRequestURL();
    }

}
