package com.scnu.gulimall.seckill.scheduled;

import com.scnu.gulimall.seckill.constant.RedisConstant;
import com.scnu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品上架:
 *      每天时间空闲的时候(深夜) 上架最近需要秒杀的商品
 */
@Slf4j
@Component
public class SeckillSkuSchedule {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redisson;

    /**
     * 每天晚上三点上架最近三天需要秒杀的商品,重复上架的商品无需处理
     *
     *
     * 幂等性保证:
     *  分布式锁先锁住秒杀上架的方法,然后再通过redis判断是否存在了这个秒杀商品相应的key,来保证同一个秒杀商品只被上架一次
     */
    @Scheduled(cron = "0 * 12 * * ?")
    public void uploadSecKillSkuLastThreeDay(){
        RLock lock = redisson.getLock(RedisConstant.UPLOAD_LOCK);
        try{
            lock.lock(10, TimeUnit.SECONDS);
            log.info("秒杀商品开始上架...");
            seckillService.uploadSecKillSkuLastThreeDay();
            log.info("上架完毕...");
        }finally {
            lock.unlock();
        }

    }

}
