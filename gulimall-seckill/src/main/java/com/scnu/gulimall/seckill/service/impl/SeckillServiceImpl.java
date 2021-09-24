package com.scnu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.scnu.common.to.mq.SeckillOrderTo;
import com.scnu.common.utils.R;
import com.scnu.gulimall.seckill.constant.RedisConstant;
import com.scnu.gulimall.seckill.feign.ProductFeign;
import com.scnu.gulimall.seckill.feign.CouponFeign;
import com.scnu.gulimall.seckill.interceptor.UserInterceptor;
import com.scnu.gulimall.seckill.service.SeckillService;
import com.scnu.gulimall.seckill.to.SeckillSkuRedisTO;
import com.scnu.gulimall.seckill.vo.SeckillSessionWithSkusVO;
import com.scnu.gulimall.seckill.vo.SeckillSkuVO;
import com.scnu.gulimall.seckill.vo.SkuInfoVO;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CouponFeign couponFeign;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSecKillSkuLastThreeDay() {
        R r = couponFeign.seckillLastXDay(3);
        if(r.getCode() == 0){
            List<SeckillSessionWithSkusVO> data = r.getData("data", new TypeReference<List<SeckillSessionWithSkusVO>>() {});
            saveSession(data);
            saveSkuInfo(data);
        }
    }

    @Override
    public List<SeckillSkuRedisTO> currentSeckill() {
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(RedisConstant.CACHE_SESSION_PREFIX + "*");
        if(ObjectUtils.isEmpty(keys)){
            return null;
        }
        List<SeckillSkuRedisTO> tos = new ArrayList<>();
        for (String key : keys) {
            //seckill:session:1632412800000_1632420000000
            String timeRange = key.substring(RedisConstant.CACHE_SESSION_PREFIX.length());
            String[] s = timeRange.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            //当前时间段在这个秒杀场次
            if(startTime <= time && time <= endTime){
                List<String> range = redisTemplate.opsForList().range(key, -1000, 1000);//取所有
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(RedisConstant.CACHE_SKU_PREFIX);
                List<String> skus = ops.multiGet(range);
                //对于随机码,因此是查时间段在当前秒杀场次的,因此能查出来的数据,就是可以秒杀的数据,随机码可以显示出来.
                List<SeckillSkuRedisTO> collect =
                        skus.stream().map(sku -> JSON.parseObject(sku, SeckillSkuRedisTO.class)).collect(Collectors.toList());
                tos.addAll(collect);
            }
        }
        return tos;
    }

    @Override
    public SeckillSkuRedisTO seckillSku(Long skuId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(RedisConstant.CACHE_SKU_PREFIX);
        Set<String> keys = ops.keys();
        if(ObjectUtils.isNotEmpty(keys)){
            SeckillSkuRedisTO to = null;
            String reg = "\\d_" + skuId;
            for (String key : keys) {
                if(Pattern.matches(reg,key)){
                    String json = ops.get(key);
                    to = JSON.parseObject(json,SeckillSkuRedisTO.class);
                    long curTime = System.currentTimeMillis();
                    if(curTime >= to.getStartTime() && curTime <= to.getEndTime()){
                        //符合秒杀时间,给出秒杀的随机码
                        break;
                    }
                    else{
                        //不在秒杀时间段,隐藏随机码,不给用户秒杀商品
                        to.setRandomCode(null);
                    }

                }
            }
            //上传秒杀商品的时候,应该有一个逻辑,一个sku在一个秒杀时间段只能在一个秒杀场次,不能同时出现在多个秒杀场次
            //即8:00 - 9:00 这个sku在秒杀中,那么在8:00-9:00,有且仅有一个sku在秒杀活动中
            return to;
        }
        return null;
    }

    /**
     * 对于秒杀场景,我觉得应该:
     *  秒杀商品上架到redis的时候,对应先把库存给扣掉
     *      等商品秒杀完或者等到秒杀活动结束的时候,查看redis中的库存是否还有剩余,如果有剩余的话,补还给数据库
     *
     *      同时也可以在秒杀活动结束后,统一清除掉此次秒杀活动产生的redis数据,就没有必要在秒杀方法中设置ttl了
     *          如用户占座的ttl,可以留到秒杀活动后统一删除
     *
     *  老师的逻辑没有解决用户可能多次秒杀的问题
     *  解决方案:
     *      1.lua脚本
     *          redis抢信号量[库存],用户占座写在一个lua脚本里面,保证它们是原子操作
     *      2.分布式锁
     *  这里采用redisson分布式锁解决
     */
    @Override
    public String seckill(String killId, String key, Integer num) {
        SeckillSkuRedisTO to = isValidate(killId,key,num);
        if(to != null){
            RLock lock = redisson.getLock(RedisConstant.SECKILL_LOCK);
            lock.lock(10, TimeUnit.SECONDS);
            try{
                //判断此用户是否秒杀过
                String userKey = RedisConstant.CACHE_USER_SECKILL_PREFIX + UserInterceptor.userInfoThreadLocal.get().getId() + "_" + to.getPromotionSessionId() + "_" + to.getSkuId();
                long ttl = to.getEndTime() - System.currentTimeMillis();
                Boolean checkUser = redisTemplate.opsForValue().setIfAbsent(userKey, String.valueOf(num),ttl,TimeUnit.MILLISECONDS);
                if(checkUser) {
                    //此时可以尝试秒杀[redis抢信号量]
                    RSemaphore semaphore = redisson.getSemaphore(RedisConstant.CACHE_STOCK_PREFIX + key);
                    boolean kill = semaphore.tryAcquire(num);
                    if (kill) {
                        //秒杀成功 发消息给mq
                        SeckillOrderTo orderTo = new SeckillOrderTo();
                        String orderSn = IdWorker.getTimeId();
                        orderTo.setOrderSn(orderSn);
                        orderTo.setSeckillPrice(to.getSeckillPrice());
                        orderTo.setMemberId(UserInterceptor.userInfoThreadLocal.get().getId());
                        orderTo.setNum(num);
                        orderTo.setPromotionSessionId(to.getPromotionSessionId());
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                        return orderSn;
                    }
                    else{
                        //如果没有秒杀成功,还需要删除redis中保存的用户对应的key
                        redisTemplate.delete(userKey);
                    }
                }
            }
            finally {
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 按照我的思路:
     *  我觉得应该,每个判断失败的话,直接抛出对应的判断异常,写一个ControllerAdvice接受异常比较合理
     */
    private SeckillSkuRedisTO isValidate(String killId, String key, Integer num) {
        //数量不能小于等于0
        if(num <= 0){
            return null;
        }
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(RedisConstant.CACHE_SKU_PREFIX);
        //判断商品是否存在
        String json = ops.get(killId);
        if(StringUtils.isEmpty(json)){
            return null;
        }
        SeckillSkuRedisTO to = JSON.parseObject(json,SeckillSkuRedisTO.class);
        //商品的随机码判断
        if(!to.getRandomCode().equals(key)){
            return null;
        }
        //判断当前秒杀时间是否正确
        long curTime = System.currentTimeMillis();
        if(!(curTime >= to.getStartTime() && curTime <= to.getEndTime())){
            return null;
        }
        //超过限购数量
        if(to.getSeckillLimit() < num){
            return null;
        }
        return to;
    }

    /**
     * 缓存sku数据
     *
     * 由于不同的秒杀场次可能上架同一款sku商品,因此保存key的时候需要通过sessionId判断这个sku商品是哪个秒杀场次的
     *
     * @param data
     */
    private void saveSkuInfo(List<SeckillSessionWithSkusVO> data) {
        if(ObjectUtils.isEmpty(data)){
            return;
        }
        data.forEach(session -> {
            List<SeckillSkuVO> relationSkus = session.getRelationSkus();
            if(ObjectUtils.isNotEmpty(relationSkus)){
                HashOperations<String, String, String> ops = redisTemplate.opsForHash();
                relationSkus.forEach(relationSku -> {
                    String hashKey = session.getId() + "_" + relationSku.getSkuId();
                    /**
                     * 上架前需要判断这个key是否已经存在了,即被上架了的
                     * sku数据和秒杀商品的数量这个分布式信号量是一体的
                     */
                    if(!ops.hasKey(RedisConstant.CACHE_SKU_PREFIX,hashKey)){
                        SeckillSkuRedisTO to = new SeckillSkuRedisTO();
                        //1.sku的秒杀信息
                        BeanUtils.copyProperties(relationSku,to);
                        //2.sku的详细信息
                        R info = productFeign.info(relationSku.getSkuId());
                        if(info.getCode() == 0) {
                            SkuInfoVO skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                            });
                            to.setSkuInfoVo(skuInfo);
                        }
                        //3.当前秒杀商品的开始和结束时间
                        to.setStartTime(session.getStartTime().getTime());
                        to.setEndTime(session.getEndTime().getTime());
                        //4.商品随机码 防止别人知道了skuId之后直接进行秒杀,商品随机码等到秒杀活动开始后才透露
                        String token = UUID.randomUUID().toString().replace("-","");
                        to.setRandomCode(token);

                        RSemaphore semaphore = redisson.getSemaphore(RedisConstant.CACHE_STOCK_PREFIX + token);
                        //商品可以秒杀的数量作为信号量
                        semaphore.trySetPermits(relationSku.getSeckillCount());

                        ops.put(RedisConstant.CACHE_SKU_PREFIX,hashKey,JSON.toJSONString(to));
                    }
                });
            }
        });
    }

    /**
     * 缓存秒杀场次的信息
     * key: startTime_endTime
     * value: [sessionId_skuId, ...]
     *
     * 由于不同的秒杀场次可能上架同一款sku商品,因此存值的时候需要通过sessionId判断这个sku商品是哪个秒杀场次的
     * @param data
     */
    private void saveSession(List<SeckillSessionWithSkusVO> data) {
        if(ObjectUtils.isEmpty(data)){
            return;
        }
        data.forEach(session -> {
            List<SeckillSkuVO> relationSkus = session.getRelationSkus();
            if(ObjectUtils.isNotEmpty(relationSkus)){
                long startTime = session.getStartTime().getTime();
                long entTime = session.getEndTime().getTime();
                String key = RedisConstant.CACHE_SESSION_PREFIX + startTime + "_" + entTime;
                if(!redisTemplate.hasKey(key)) {
                    List<String> skuIds = relationSkus.stream().map(relationSku -> session.getId() + "_" + relationSku.getSkuId()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, skuIds);
                }
            }
        });
    }
}
