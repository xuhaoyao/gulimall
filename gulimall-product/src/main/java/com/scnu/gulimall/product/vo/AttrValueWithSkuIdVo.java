package com.scnu.gulimall.product.vo;

import lombok.Data;

/**
 * 用来进行sku的组合切换
 */
@Data
public class AttrValueWithSkuIdVo {

    private String attrValue;
    private String skuIds;

}
