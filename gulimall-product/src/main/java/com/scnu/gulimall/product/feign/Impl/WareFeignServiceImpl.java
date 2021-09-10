package com.scnu.gulimall.product.feign.Impl;

import com.scnu.common.to.SkuHasStockTo;
import com.scnu.gulimall.product.feign.WareFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WareFeignServiceImpl implements WareFeignService {
    @Override
    public List<SkuHasStockTo> hasStock(List<Long> skuIds) {
        log.error("远程调用失败");
        return null;
    }
}
