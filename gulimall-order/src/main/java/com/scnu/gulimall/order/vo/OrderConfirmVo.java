package com.scnu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    //防重令牌
    @Getter @Setter
    private String orderToken;

    //收货地址  ums_member_receive_address
    @Getter @Setter
    private List<MemberAddressVo> address;

    //所有选中的购物项
    @Getter @Setter
    private List<OrderItemVo> items;

    //发票...

    //优惠卷信息
    @Getter @Setter
    private Integer integration;

    //订单总额
    //private BigDecimal total;
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(!ObjectUtils.isEmpty(items)){
            sum = items.stream().map(OrderItemVo::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return sum;
    }

    //应付总额
    //private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }


    public Integer getAllCount(){
        if(!ObjectUtils.isEmpty(items)){
            return items.stream().map(OrderItemVo::getCount).reduce(0,Integer::sum);
        }
        return 0;
    }

    //是否有货物  尝试一下写法 [[${orderConfirmData.hasStock[item.skuId]}]]
    @Getter @Setter
    private Map<Long,Boolean> hasStock;
}
