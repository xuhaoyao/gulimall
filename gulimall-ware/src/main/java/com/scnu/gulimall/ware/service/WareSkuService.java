package com.scnu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.to.mq.StockLockedTo;
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

    void releaseStock(StockLockedTo to);

    /**
     * 解锁库存的同时,更新wms_ware_order_task_detail的锁定状态
     * @param skuId
     * @param wareId
     * @param skuNum
     * @param detailId
     */
    void upLockedStock(Long skuId,Long wareId,Integer skuNum,Long detailId);

    /**
     * 根据此订单号解锁相应的库存
     * @param orderSn
     */
    void releaseStock(String orderSn);
}

