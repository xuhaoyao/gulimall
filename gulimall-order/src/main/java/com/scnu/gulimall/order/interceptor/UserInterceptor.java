package com.scnu.gulimall.order.interceptor;

import com.scnu.common.constant.auth.AuthConstant;
import com.scnu.common.vo.UserInfoVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoVo> userInfoThreadLocal = new ThreadLocal<>();

    /**
     * 去结算之间要判断用户是否已经登录
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
