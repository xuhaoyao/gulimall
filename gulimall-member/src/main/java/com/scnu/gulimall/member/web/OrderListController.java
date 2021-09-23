package com.scnu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.scnu.common.utils.R;
import com.scnu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OrderListController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/orderList.html")
    public String orderListPage(@RequestParam(required = false,defaultValue = "1") String pageNum, Model model){
        Map<String,Object> params = new HashMap<>();
        params.put("page",pageNum);
        R r = orderFeignService.memberOrderList(params);
        model.addAttribute("orders",r.get("page"));
        System.out.println(JSON.toJSONString(r.get("page")));
        return "orderList";
    }

}
