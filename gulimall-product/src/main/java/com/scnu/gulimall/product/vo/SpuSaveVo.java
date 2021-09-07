/**
 * Copyright 2019 bejson.com
 */
package com.scnu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2019-11-26 10:50:34
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    //spu基本信息  pms_spu_info
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;

    //图片描述(介绍这款手机用)  pms_spu_info_desc
    private List<String> decript;
    //图片展示(类似于购买页面可以滑动的幻灯片) pms_spu_images
    private List<String> images;

    //成长积分,购物积分 gulimall_sms -> sms_spu_bounds
    private Bounds bounds;
    //spu规格参数 pms_product_attr_value
    private List<BaseAttrs> baseAttrs;
    //销售参数
    private List<Skus> skus;



}