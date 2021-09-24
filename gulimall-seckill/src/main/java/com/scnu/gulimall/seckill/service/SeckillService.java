package com.scnu.gulimall.seckill.service;

import com.scnu.gulimall.seckill.to.SeckillSkuRedisTO;

import java.util.List;

public interface SeckillService {

    /**
     * 给redis存入最近三天的秒杀商品
     */
    void uploadSecKillSkuLastThreeDay();

    /**
     * 从redis查当前时间段的所有秒杀商品
     * @return
     */
    List<SeckillSkuRedisTO> currentSeckill();

    /**
     * 查出当前秒杀场次是否有这个sku
     * @param skuId
     * @return
     */
    SeckillSkuRedisTO seckillSku(Long skuId);

    /**
     * 秒杀商品
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String seckill(String killId, String key, Integer num);
}
