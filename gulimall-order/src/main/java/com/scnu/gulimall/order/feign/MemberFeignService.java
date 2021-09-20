package com.scnu.gulimall.order.feign;

import com.scnu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/member/orderConfrimInfo/{id}")
    public R orderConfrimInfo(@PathVariable("id") Long id);

}
