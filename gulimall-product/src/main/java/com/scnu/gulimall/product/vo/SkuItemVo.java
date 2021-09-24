package com.scnu.gulimall.product.vo;

import com.scnu.gulimall.product.entity.SkuImagesEntity;
import com.scnu.gulimall.product.entity.SkuInfoEntity;
import com.scnu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    //1.获取sku基本信息 pms_sku_info
    private SkuInfoEntity info;
    //2.获取sku图片信息 pms_sku_images
    private List<SkuImagesEntity> images;
    //3.获取spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttrs;
    //4.获取spu的介绍
    private SpuInfoDescEntity desc;
    //5.获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6.TODO
    private Boolean hasStock = true;

    //7.当前sku的秒杀信息
    private SeckillSkuVO seckillSkuVO;

}
