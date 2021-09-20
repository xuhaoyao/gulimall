package com.scnu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.ware.entity.WareSkuEntity;
import com.scnu.gulimall.ware.exception.NoStockException;
import com.scnu.gulimall.ware.vo.LockStockResult;
import com.scnu.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:21:28
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> hasStock(List<Long> skuIds);

    /**
     * 订单服务远程调用,锁定库存
     * @param vo
     * @return
     */
    void orderLockStock(WareSkuLockVo vo) throws NoStockException;
}

