package com.scnu.gulimall.order.feign;

import com.scnu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @ResponseBody
    @GetMapping("/userCartItemsInfo")
    public List<OrderItemVo> userCartItemsInfo();

}
