package com.scnu.gulimall.product.web;

import com.scnu.gulimall.product.entity.CategoryEntity;
import com.scnu.gulimall.product.service.CategoryService;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities = categoryService.getLevelOneList();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,Object> catalog(){
        Map<String,Object> map = categoryService.getcatalogJson();
        return map;
    }

    /**
     * 如果负责储存这个分布式锁的Redisson节点宕机以后，而且这个锁正好处于锁住的状态时，
     * 这个锁会出现锁死的状态。为了避免这种情况的发生，Redisson内部提供了一个监控锁的看门狗，
     * 它的作用是在Redisson实例被关闭前，不断的延长锁的有效期。
     * 默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定。
     *
     * 另外Redisson还通过加锁的方法提供了leaseTime的参数来指定加锁的时间。超过这个时间后锁便自动解开了。
     */
    @ResponseBody
    @GetMapping("/reentrantLock")
    public String reentrantLock(){

        RLock rLock = redisson.getLock("reentrantLock");
        //this.lockWatchdogTimeout = 30000L; 没指定时间的话默认锁30s
        rLock.lock();   //阻塞式等待

        //rLock.lock(10L,TimeUnit.SECONDS); //10s自动解锁,不会自动续期,这个时候解锁就会有异常
        /**
         * rLock.lock(10L,TimeUnit.SECONDS);
         * 1.如果我们传递了锁的超时时间,就发送给redis一个lua脚本,进行占锁,默认超时就是我们指定的时间
         * 2.如果我们未指定超时时间,默认30s
         *      只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间,新的过期时间就是看门狗的默认时间】,每隔(30 / 3)s,执行一次定时任务
         * 3.最佳实战
         *      rLock.lock(30L,TimeUnit.SECONDS);
         *      省掉了整个续期操作,手动解锁
         */
        try {
            System.out.println("执行业务--->" + Thread.currentThread().getId());
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) { e.printStackTrace(); }
        finally {
            System.out.println("解锁成功--->" + Thread.currentThread().getId());
            rLock.unlock();
        }
        return "reentrantLock";
    }

    /**
     * 写锁是排他锁,读锁是共享锁
     * 写锁没释放读就必须等待
     * 读 + 读 :相当于无锁,并发读,只会在redis记录好当前所有的读锁,它们都会同时加锁成功
     * 读 + 写 : 有读锁,写锁也需要等待
     * 写 + 写 : 阻塞方式
     * 写 + 读 : 等待写锁释放
     */
    @ResponseBody
    @GetMapping("/writeLock")
    public String writeLock(){
        RReadWriteLock rReadWriteLock = redisson.getReadWriteLock("rw-lock");
        String uuid = UUID.randomUUID().toString();
        RLock lock = rReadWriteLock.writeLock();
        try {
            lock.lock();
            System.out.println("写锁加锁成功--->" + Thread.currentThread().getId());
            try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
            stringRedisTemplate.opsForValue().set("rw-lock-value",uuid);
        }finally {
            lock.unlock();
        }
        return uuid;
    }

    @ResponseBody
    @GetMapping("/readLock")
    public String readLock(){
        RReadWriteLock rReadWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock lock = rReadWriteLock.readLock();
        String value;
        try {
            System.out.println("读锁加锁成功--->" + Thread.currentThread().getId());
            lock.lock();
            value = stringRedisTemplate.opsForValue().get("rw-lock-value");
        }finally {
            lock.unlock();
        }
        return value;
    }

    /**
     * 模拟5个人离开才关门
     */
    @ResponseBody
    @GetMapping("/door")
    public String door(){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        try {
            door.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "关门";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") String id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();
        return id + "离开";
    }

}
