package com.scnu.gulimall.product.dao;

import com.scnu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.gulimall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:57
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {


    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    List<String> saleAttrs(Long skuId);
}
