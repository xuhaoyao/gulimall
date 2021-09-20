package com.scnu.gulimall.order.to;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuHasStockTo implements Serializable {
    private Long skuId;

    private Boolean hasStock;
}
