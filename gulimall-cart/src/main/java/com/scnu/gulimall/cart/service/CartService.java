package com.scnu.gulimall.cart.service;

import com.scnu.gulimall.cart.vo.CartItemVo;
import com.scnu.gulimall.cart.vo.CartVo;

import java.util.List;

public interface CartService {
    /**
     * 将商品添加进购物车
     * @param skuId  商品id
     * @param num    商品数量
     * @return
     */
    CartItemVo addToCart(Long skuId, Integer num);

    /**
     * 商品添加进购物车后,重定向到添加成功页面,查添加成功的商品
     * @param skuId
     * @return
     */
    CartItemVo getItemById(String skuId);

    /**
     * 获取当前用户的购物车数据
     * 登录状态下:合并临时购物车与用户购物车
     * 未登录下:  获取临时购物车
     * @return
     */
    CartVo getCart();

    /**
     * 删除购物车
     * @param cartKey
     */
    void deleteCart(String cartKey);

    /**
     * 改变购物车的选中状态
     * @param skuId
     * @param checked
     */
    void changeCheck(Long skuId, Boolean checked);

    /**
     * 改变购物项的数量
     * @param skuId
     * @param count
     */
    void changeCount(Long skuId, Integer count);

    /**
     * 删除购物车的一个选项
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 查当前登录用户选中的购物项信息
     * @return
     */
    List<CartItemVo> userCartItemsInfo();
}
