package com.scnu.gulimall.cart.controller;

import com.scnu.gulimall.cart.constant.AuthConstant;
import com.scnu.gulimall.cart.interceptor.CartInterceptor;
import com.scnu.gulimall.cart.service.CartService;
import com.scnu.gulimall.cart.to.UserInfoTo;
import com.scnu.gulimall.cart.vo.CartItemVo;
import com.scnu.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/userCartItemsInfo")
    public List<CartItemVo> userCartItemsInfo(){
        return cartService.userCartItemsInfo();
    }

    /**
     * 浏览器有一个cookie,name=user-key,标识用户信息
     * 如果第一次使用购物车,分发一个临时的user-key
     * 浏览器以后每次访问都会带上这个user-key
     *
     * 登录状态下:session有用户信息
     * 没登陆:   根据cookie里面的user-key来查询购物车
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model){
        CartVo cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.addToCart(skuId,num);
        //return "success";  //存在表单重复提交的问题
        return "redirect:http://cart.gulimall.com/success.html?skuId=" + skuId;
    }

    @GetMapping("/success.html")
    public String successToCart(@RequestParam("skuId") String skuId,Model model){
        CartItemVo item = cartService.getItemById(skuId);
        model.addAttribute("item",item);
        return "success";
    }

    @GetMapping("/changeCheck")
    public String changeCheck(@RequestParam("skuId") Long skuId,@RequestParam("checked") Boolean checked){
        cartService.changeCheck(skuId,checked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/changeCount")
    public String changeCount(@RequestParam("skuId") Long skuId,@RequestParam("count") Integer count){
        //TODO 此处应该检查count是否为负数
        cartService.changeCount(skuId,count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
