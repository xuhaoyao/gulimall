package com.scnu.gulimall.seckill.feign;

import com.scnu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-coupon")
public interface CouponFeign {

    @GetMapping("/coupon/seckillsession/seckillLastXDay/{x}")
    public R seckillLastXDay(@PathVariable("x") Integer x);

}
