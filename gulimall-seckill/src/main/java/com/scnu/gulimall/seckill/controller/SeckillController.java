package com.scnu.gulimall.seckill.controller;

import com.scnu.common.utils.R;
import com.scnu.gulimall.seckill.service.SeckillService;
import com.scnu.gulimall.seckill.to.SeckillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/currentSeckill")
    public R currentSeckill(){
        List<SeckillSkuRedisTO> tos = seckillService.currentSeckill();
        return R.ok().put("data",tos);
    }

    @GetMapping("/seckillSku/{skuId}")
    public R seckillSku(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTO to = seckillService.seckillSku(skuId);
        return R.ok().put("data",to);
    }

    @GetMapping("/seckill")
    public R seckill(@RequestParam(value = "killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num){
        String orderSn = seckillService.seckill(killId,key,num);
        return R.ok().put("orderSn",orderSn);
    }

}
