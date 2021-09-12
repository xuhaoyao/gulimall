package com.scnu.gulimall.search.feign.impl;

import com.scnu.common.utils.R;
import com.scnu.gulimall.search.feign.ProductFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductFeignServiceImpl implements ProductFeignService {
    @Override
    public R info(Long attrId) {
        log.error("远程调用获取attr的信息失败");
        return R.error();
    }
}
