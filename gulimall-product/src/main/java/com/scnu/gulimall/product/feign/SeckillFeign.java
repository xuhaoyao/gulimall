package com.scnu.gulimall.product.feign;

import com.scnu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SeckillFeign {

    @GetMapping("/seckillSku/{skuId}")
    public R seckillSku(@PathVariable("skuId") Long skuId);

}
