package com.scnu.gulimall.product.feign;

import com.scnu.common.to.SkuHasStockTo;
import com.scnu.gulimall.product.feign.Impl.WareFeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "gulimall-ware",fallback = WareFeignServiceImpl.class)
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasStock")
    List<SkuHasStockTo> hasStock(@RequestBody List<Long> skuIds);
}
