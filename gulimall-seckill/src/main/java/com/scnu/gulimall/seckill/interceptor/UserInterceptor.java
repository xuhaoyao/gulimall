package com.scnu.gulimall.seckill.interceptor;

import com.scnu.common.constant.auth.AuthConstant;
import com.scnu.common.vo.UserInfoVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoVo> userInfoThreadLocal = new ThreadLocal<>();

    /**
     * 此拦截器只拦截用户的秒杀请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoVo userInfo = (UserInfoVo) request.getSession().getAttribute(AuthConstant.SESSION_USER_NAME);
        if(userInfo == null){
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        userInfoThreadLocal.set(userInfo);
        return true;
    }
}
