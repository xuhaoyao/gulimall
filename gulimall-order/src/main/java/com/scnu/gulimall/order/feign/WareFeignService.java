package com.scnu.gulimall.order.feign;

import com.scnu.common.to.SkuHasStockTo;
import com.scnu.common.utils.R;
import com.scnu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    public List<SkuHasStockTo> hasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/addressFee/{addrId}")
    public R addressFee(@PathVariable("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);

}
