package com.scnu.gulimall.search.feign;

import com.scnu.common.utils.R;
import com.scnu.gulimall.search.feign.impl.ProductFeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimall-product",fallback = ProductFeignServiceImpl.class)
public interface ProductFeignService {

    @RequestMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

}
