package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:58
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getSkuImagesBySkuId(Long skuId);
}

