package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.SkuInfoEntity;
import com.scnu.gulimall.product.vo.SkuItemVo;
import com.scnu.gulimall.product.vo.SpuSaveVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils querySkuInfo(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * 商品详情页跳转时:根据skuId查出页面需要展示的数据
     * @param skuId
     * @return
     */
    SkuItemVo item(Long skuId);
}

