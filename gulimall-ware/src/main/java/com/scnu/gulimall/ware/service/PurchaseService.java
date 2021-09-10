package com.scnu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.ware.entity.PurchaseEntity;
import com.scnu.gulimall.ware.vo.PurchaseDoneVo;
import com.scnu.gulimall.ware.vo.PurchaseMergeVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:21:29
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询未领取的采购单
     * @return
     */
    PageUtils unreceiveList(Map<String, Object> params);

    /**
     * 合并订单操作
     * @param vo
     */
    void merge(PurchaseMergeVo vo);

    /**
     * 采购员领取采购单
     * @param ids
     */
    void received(List<Long> ids);

    /**
     * 采购员完成采购
     * @param purchaseDoneVo
     */
    void done(PurchaseDoneVo purchaseDoneVo);
}

