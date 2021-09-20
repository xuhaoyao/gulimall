package com.scnu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {

    private Long skuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> skuAttrs;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    //TODO
    private Boolean hasStock;
    private BigDecimal weight;


}
