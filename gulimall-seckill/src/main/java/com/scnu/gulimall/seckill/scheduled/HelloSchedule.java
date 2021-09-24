package com.scnu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务:
 *  1.@EnableScheduling 开启定时任务
 *  2.@Scheduled(cron = "...") 给方法标注,表示它是一个定时任务
 *  3.自动配置类  TaskSchedulingAutoConfiguration
 *      TaskSchedulingProperties [prefix = "spring.task.scheduling"]
 *      可以配置线程池数量,但是配了之后不起作用,定时任务还是会阻塞,隔两秒才执行
 *
 * 异步任务:
 *  1.@EnableAsync 开启异步任务
 *  2.@Async       表示这个方法是异步方法
 *  3.TaskExecutionAutoConfiguration 自动配置类
 *      TaskExecutionProperties [prefix = "spring.task.execution"]
 *    如果要开异步任务的话,一定要手动配置它的线程池,因为默认队列和最大线程数量都是Integer.MAX_VALUE
 */
@Slf4j
@Component
public class HelloSchedule {

    /**
     * . @Scheduled(cron = "* * * * * ?")
     *  cron表达式,在spring中:
     *  1. 6位组成 不包含第7位的年
     *  2.定时任务不应该是阻塞的,但它默认就是阻塞的
     *      a.可以让业务以异步的方式进行,提交给线程池
     *      b. CompletableFuture.runAsync( () -> {},pool )
     *      c.定时任务也有它自己的线程池,默认线程是1个,但是配成5个没有起作用??
     *      d.让定时任务以异步执行 @Async -> 注意配好它默认的线程池参数
     *
     *  使用异步+定时任务来完成定时任务不阻塞的功能
     */
/*    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello(){
        log.info("hello...");
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
    }*/

}
