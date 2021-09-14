package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.scnu.gulimall.product.vo.SkuItemSaleAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:57
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 商品详情页:根据spuId查询sku所有的销售属性的组合
     * @param spuId
     * @return
     */
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);
}

