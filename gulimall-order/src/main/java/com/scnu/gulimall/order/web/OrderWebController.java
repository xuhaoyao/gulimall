package com.scnu.gulimall.order.web;

import com.scnu.gulimall.order.service.OrderService;
import com.scnu.gulimall.order.vo.OrderConfirmVo;
import com.scnu.gulimall.order.vo.OrderFormVo;
import com.scnu.gulimall.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo vo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",vo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderFormVo orderFormVo,Model model){

        SubmitOrderRespVo vo = orderService.submitOrder(orderFormVo);
        if(vo.getCode() == 200){
            //下单成功,去支付页
            model.addAttribute("submitOrderResp",vo);
            return "pay";
        }else{
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}
