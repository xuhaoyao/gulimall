package com.scnu.gulimall.auth.feign;

import com.scnu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @PostMapping("/sendCode")
    R sendCode(@RequestParam("email") String email, @RequestParam("code") String code);

}
