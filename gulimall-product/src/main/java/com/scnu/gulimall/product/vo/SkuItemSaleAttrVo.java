package com.scnu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuItemSaleAttrVo {
    private String attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
