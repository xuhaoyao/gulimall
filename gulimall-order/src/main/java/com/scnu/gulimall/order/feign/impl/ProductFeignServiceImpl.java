package com.scnu.gulimall.order.feign.impl;

import com.scnu.common.utils.R;
import com.scnu.gulimall.order.feign.ProductFeignService;
import com.scnu.gulimall.order.vo.SpuInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class ProductFeignServiceImpl implements ProductFeignService {
    @Override
    public R getSpuInfoBySkuId(Long id) {
        log.error("远程调用商品服务:--->取spu失败");
        return R.error().put("data",new SpuInfoEntity());
    }
}
