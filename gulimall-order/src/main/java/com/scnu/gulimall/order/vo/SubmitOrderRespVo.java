package com.scnu.gulimall.order.vo;

import com.scnu.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderRespVo {

    private OrderEntity order;
    private Integer code;

}
