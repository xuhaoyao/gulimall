package com.scnu.gulimall.order.to;

import com.scnu.gulimall.order.entity.OrderEntity;
import com.scnu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity order;
    private List<OrderItemEntity> items;
    private BigDecimal payPrice; //订单计算的应付价格
    private BigDecimal fare;     //运费

}
