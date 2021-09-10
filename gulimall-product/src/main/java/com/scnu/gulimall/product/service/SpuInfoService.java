package com.scnu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.product.entity.SpuInfoEntity;
import com.scnu.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 13:07:57
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 大保存,发布商品->保存商品信息
     * @param vo
     */
    void saveSpuInfo(SpuSaveVo vo);

    PageUtils querySpuPage(Map<String, Object> params);

    /**
     * 商品上架
     */
    void up(Long spuId);
}

