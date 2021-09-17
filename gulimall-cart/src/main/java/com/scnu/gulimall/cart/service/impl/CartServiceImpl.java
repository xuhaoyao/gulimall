package com.scnu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scnu.common.utils.R;
import com.scnu.gulimall.cart.constant.CartConstant;
import com.scnu.gulimall.cart.feign.ProductFeignService;
import com.scnu.gulimall.cart.interceptor.CartInterceptor;
import com.scnu.gulimall.cart.service.CartService;
import com.scnu.gulimall.cart.to.SkuInfoTo;
import com.scnu.gulimall.cart.to.UserInfoTo;
import com.scnu.gulimall.cart.vo.CartItemVo;
import com.scnu.gulimall.cart.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ExecutorService pool;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) {

        BoundHashOperations<String, String, String> cartOps = getRedisCart();
        String itemJson = cartOps.get(skuId.toString());
        CartItemVo item = JSON.parseObject(itemJson,CartItemVo.class);
        //购物车里没有商品,新增商品
        if(item == null){
            return addCartItem(skuId, num, cartOps);
        }
        //购物车里有商品了,增加商品数量即可
        else{
            item.setCount(item.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(item));
            return item;
        }

    }

    @Override
    public CartItemVo getItemById(String skuId) {
        BoundHashOperations<String, String, String> redisCart = getRedisCart();
        String itemJson = redisCart.get(skuId);
        return JSON.parseObject(itemJson,CartItemVo.class);
    }

    @Override
    public CartVo getCart() {
        CartVo cart = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String tmpCartkey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        String userCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();

        if(userInfoTo.getUserId() != null){
            //用户登录了,先获取临时购物车合并到用户购物车,然后删除掉临时购物车,然后再获取用户购物车
            List<CartItemVo> tmpCartItems = getCartItemsByCaryKey(tmpCartkey);
            if(!ObjectUtils.isEmpty(tmpCartItems)) {
                for (CartItemVo tmpCartItem : tmpCartItems) {
                    //将临时物品添加进购物车
                    addToCart(tmpCartItem.getSkuId(), tmpCartItem.getCount());
                }
                deleteCart(tmpCartkey);
            }
            List<CartItemVo> userCartItems = getCartItemsByCaryKey(userCartKey);
            cart.setItems(userCartItems);
        }
        else{
            //用户没有登录
            List<CartItemVo> tmpCartItems = getCartItemsByCaryKey(tmpCartkey);
            cart.setItems(tmpCartItems);
        }
        return cart;
    }

    private List<CartItemVo> getCartItemsByCaryKey(String cartKey){
        BoundHashOperations<String, String, String> tmpOps = redisTemplate.boundHashOps(cartKey);
        List<String> tmpCartItems = tmpOps.values();
        if(!ObjectUtils.isEmpty(tmpCartItems)){
            List<CartItemVo> collect = tmpCartItems.stream().map(item -> {
                CartItemVo cartItemVo = JSON.parseObject(item, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void deleteCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void changeCheck(Long skuId, Boolean checked) {
        BoundHashOperations<String, String, String> redisCart = getRedisCart();
        CartItemVo item = getItemById(skuId.toString());
        item.setCheck(checked);
        redisCart.put(skuId.toString(),JSON.toJSONString(item));
    }

    @Override
    public void changeCount(Long skuId, Integer count) {
        BoundHashOperations<String, String, String> redisCart = getRedisCart();
        CartItemVo item = getItemById(skuId.toString());
        item.setCount(count);
        redisCart.put(skuId.toString(),JSON.toJSONString(item));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, String, String> redisCart = getRedisCart();
        redisCart.delete(skuId.toString());
    }

    /**
     * 给购物车新增一个购物项
     * @param skuId   商品id
     * @param num     商品数量
     * @param cartOps 操作redis的购物车
     * @return
     */
    private CartItemVo addCartItem(Long skuId, Integer num, BoundHashOperations<String, String, String> cartOps) {
        //封装数据
        CartItemVo vo = new CartItemVo();
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            //远程查询要添加的商品信息
            R r = productFeignService.getSkuInfo(skuId);
            SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {
            });
            vo.setCheck(true);
            vo.setCount(num);
            vo.setImage(skuInfo.getSkuDefaultImg());
            vo.setPrice(skuInfo.getPrice());
            vo.setTitle(skuInfo.getSkuTitle());
            vo.setSkuId(skuId);
        }, pool);

        CompletableFuture<Void> saleAttrsFuture = CompletableFuture.runAsync(() -> {
            //查询sku的组合信息
            vo.setSkuAttrs(productFeignService.saleAttrs(skuId));
        }, pool);

        try {
            CompletableFuture.allOf(skuInfoFuture,saleAttrsFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        cartOps.put(skuId.toString(), JSON.toJSONString(vo));
        return vo;
    }

    /**
     * 根据用户的信息拿到存在redis中的购物车
     * @return
     */
    private BoundHashOperations<String, String, String> getRedisCart() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PREFIX;
        if(userInfoTo.getUserId() != null){
            //用户购物车
            cartKey += userInfoTo.getUserId();
        }
        else{
            //临时购物车
            cartKey += userInfoTo.getUserKey();
        }
        BoundHashOperations<String, String, String> hops = redisTemplate.boundHashOps(cartKey);
        return hops;
    }
}
