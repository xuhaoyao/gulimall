package com.scnu.gulimall.cart.constant;

public interface CartConstant {

    String USER_KEY = "user-key";
    String COOKIE_DOMAIN = "gulimall.com";
    int COOKIE_TIME = 60 * 60 * 24 * 30;   //30天

    String CART_PREFIX = "gulimall:cart::";   //redis中购物车的前缀

}
