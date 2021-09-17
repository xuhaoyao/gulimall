package com.scnu.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 */
public class CartVo {

    private List<CartItemVo> items;

    private Integer countNum; //商品数量

    private Integer countType; //商品类型数量

    private BigDecimal totalAmount; //商品总价

    private BigDecimal reduce = new BigDecimal("0"); //减免价格

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int countNum = 0;
        if(!ObjectUtils.isEmpty(items)){
            //countNum = items.stream().map(CartItemVo::getCount).reduce(0,Integer::sum);
            countNum = items.stream().mapToInt(CartItemVo::getCount).sum();
        }
        return countNum;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        int countType = 0;
        if(!ObjectUtils.isEmpty(items)){
            countType = items.size();
        }
        return countType;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        if(!ObjectUtils.isEmpty(items)){
            totalAmount = items.stream().map(CartItemVo::getTotalPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
        }
        //还要减去优惠价格,这里优惠价格不处理
        return totalAmount.subtract(getReduce());
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
