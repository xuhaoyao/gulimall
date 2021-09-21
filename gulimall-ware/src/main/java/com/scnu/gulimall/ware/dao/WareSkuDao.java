package com.scnu.gulimall.ware.dao;

import com.scnu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:21:28
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Integer hasStock(Long skuId);

    Long selectWareIdHasStockBySkuId(@Param("skuId") Long skuId, @Param("count") Integer count);

    void updateStockLock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);

    void releaseStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
