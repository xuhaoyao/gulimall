package com.scnu.common.to.mq;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillOrderTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderSn;
    private Long memberId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;


}
