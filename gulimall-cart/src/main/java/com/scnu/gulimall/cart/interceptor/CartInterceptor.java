package com.scnu.gulimall.cart.interceptor;

import com.scnu.common.vo.UserInfoVo;
import com.scnu.gulimall.cart.constant.AuthConstant;
import com.scnu.gulimall.cart.constant.CartConstant;
import com.scnu.gulimall.cart.to.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前判断用户的登录状态,并封装用户信息传递给controller
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        UserInfoVo vo = (UserInfoVo) session.getAttribute(AuthConstant.SESSION_USER_NAME);
        UserInfoTo to = new UserInfoTo();
        if(vo != null){
            //用户登录过了
            to.setUserId(vo.getId());
        }
        //通过cookie判断是否携带了user-key,即判断用户是否使用过购物车
        Cookie[] cookies = request.getCookies();
        if(!ObjectUtils.isEmpty(cookies)){
            for (Cookie cookie : cookies) {
                if(CartConstant.USER_KEY.equals(cookie.getName())){
                    to.setUserKey(cookie.getValue());
                    to.setFlag(true);
                    break;
                }
            }
        }
        //若cookie没有携带user-key,分配一个
        if(to.getUserKey() == null){
            to.setUserKey(UUID.randomUUID().toString());
        }
        threadLocal.set(to);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if(!userInfoTo.getFlag()){
            Cookie cookie = new Cookie(CartConstant.USER_KEY,userInfoTo.getUserKey());
            cookie.setDomain(CartConstant.COOKIE_DOMAIN);
            cookie.setMaxAge(CartConstant.COOKIE_TIME);
            response.addCookie(cookie);
        }
    }
}
