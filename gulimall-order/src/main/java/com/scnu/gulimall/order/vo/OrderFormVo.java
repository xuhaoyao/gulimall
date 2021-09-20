package com.scnu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderFormVo {

    private Long addrId; //收货地址id
    private Integer payType; //支付方式
    //无需提交需要购买的商品,去购物车再查一次目前勾选中的

    private String orderToken;  //防重令牌
    private BigDecimal payPrice; //应付价格,验价

    //用户相关的信息,session中拿

}
