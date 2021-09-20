package com.scnu.gulimall.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WareSkuLockVo implements Serializable {

    private String orderSn;  //订单号

    private List<OrderItemVo> locks; //需要锁住的所有库存信息
}
