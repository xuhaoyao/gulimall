package com.scnu.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scnu.common.utils.PageUtils;
import com.scnu.gulimall.ware.entity.WareInfoEntity;
import com.scnu.gulimall.ware.vo.AddressFeeVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author xhy
 * @email 623834276@qq.com
 * @date 2021-09-02 14:21:28
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收获地址id得到运费以及相应收货信息
     * @param addrId
     * @return
     */
    AddressFeeVo addressFee(Long addrId);
}

