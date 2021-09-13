package com.scnu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * 1）、继承 Thread
 *         Thread thread01 = new Thread01();
 *         thread01.start();
 * 2）、实现 Runnable 接口
 *         Thread thread02 = new Thread(new Runnable02());
 *         thread02.start();
 * 3）、实现 Callable 接口 + FutureTask （可以拿到返回结果，可以处理异常） JDK1.5
 *         FutureTask<Integer> futureTask = new FutureTask<>(new Callable03());
 *  *         new Thread(futureTask).start();
 *  *         //等待整个线程执行完成获取返回结果-->阻塞等待
 *  *         Integer integer = futureTask.get();
 *  *         System.out.println("FutureTask返回值-->" + integer);
 *  *
 *  *         Integer result = new Integer(2);
 *  *         FutureTask<Integer> integerFutureTask = new FutureTask<>(new Runnable02(), result);
 *         new Thread(integerFutureTask).start();
 *         System.out.println(integerFutureTask.get());  //2
 * 4）、线程池(ExecutorService)
 *          给线程池直接提交任务
 *          pool.execute(new Runnable02());
 *     1.创建:
 *          1)、Executors
 *              ExecutorService pool = Executors.newFixedThreadPool(10);
 *          2)、原生
 *
 *  区别:
 *      1、2不能得到返回值,3可以获取返回值
 *      1、2、3都不能控制资源
 *      4可以控制资源，性能稳定
 *
 * 以后业务代码里面应该将所有的多线程异步任务都交给线程池执行
 */
public class ThreadPoolTest {



    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println(Thread.currentThread().getName() + "启动");

        /**
         * 七大参数:
         * corePoolSize:核心线程数；线程池，创建好以后就准备就绪的线程数量，就等待来接受异步任务去执行
         *      corePoolSize = 5 -> 5个 Thread thread = new Thread();
         *      一直存在unless {@code allowCoreThreadTimeOut} is set
         * maximumPoolSize:最大线程数量;控制资源
         * keepAliveTime:存活时间.
         *      如果当前的线程数量大于corePoolSize,那么空闲的线程最多存活的时间keepAliveTime,时间一到还空闲的话,就回收这个线程
         *      释放的线程数量(maximumPoolSize-corePoolSize)
         * unit:the time unit for the {@code keepAliveTime} argument
         * BlockingQueue<Runnable> workQueue:阻塞队列
         *      如果任务有很多,就会将目前多的任务放在队列里面.
         *      只要有线程空闲,就会去队列里面取出新的任务继续执行
         * threadFactory:线程的创建工厂
         * RejectedExecutionHandler handler:
         *      处理队列满了之后的逻辑,按照我们指定的拒绝策略拒绝执行任务
         *
         * 工作顺序:
         * 1)、线程池创建，准备好 core 数量的核心线程，准备接受任务
         * 2)、但调用execute()方法添加一个请求任务时,线程池会做出如下判断:
         *      2.1 如果正在运行的线程数量小于corePoolSize,那么马上创建线程运行这个任务
         *      2.2 如果正在运行的线程数量大于或等于corePoolSize,那么将这个任务放入队列
         *      2.3 如果这个时候队列满了且正在运行的线程数量还小于maximumPoolSize,那么创建非核心线程立刻运行这个任务
         *      2.4 如果队列满了且正在运行的线程数量大于或等于maximumPoolSize,那么线程池会启动饱和拒绝策略来执行
         * 3)、当一个线程完成任务时,它会从队列中取下一个任务来执行
         * 4)、当一个线程无事可做超过一定时间(keepAliveTime),线程池会判断:
         *      如果当前运行的线程数大于corePoolSize,那么这个线程就被停掉
         * 5)、特别的,当线程数量从core扩容到max时,会先执行当前到来的任务,而不是阻塞队列中的
         *
         * 题目:一个线程池 core 7； max 20 queue ：50
         *      100 并发进来怎么分配的
         *   前面来的7个可以先得到执行,然后50个进入阻塞队列,这个时候队列满了之后呢,
         *   就会开始扩容,跨容到20个,这个时候队列里面的线程不执行,而是后面来的任务先执行,再执行13个
         *   这个时候20个线程都被占用,且队列也都满了,那么就执行拒绝策略
         *   在这后面来的任务,需要看拒绝策略
         *   AbortPolicy(默认)：直接抛出RejectedExecutionException异常阻止系统正常运行
         *   CallerRunsPolicy：调用者运行，该策略既不会抛弃任务，也不会抛出异常，而是将某些任务回退给调用者，从而降低新任务的流量
         *   DiscardOldestPolicy：抛弃队列中等待最久的任务，然后把当前任务加入队列中尝试再次提交当前任务
         *   DiscardPolicy：直接丢弃任务，不予任何处理也不抛出异常。如果允许任务丢失，这是最好的一种方案
         *
         *   而队列里的任务,等有空闲线程之后还可以继续执行.
         */
        ExecutorService pool = new ThreadPoolExecutor(
                7,
                20,
                100000,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy()  //(默认)直接抛出RejectedExecutionException异常阻止系统正常运行
        );
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            pool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + "执行 --->" + finalI);
                try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            });
        }

        System.out.println(Thread.currentThread().getName() + "结束");

    }

    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "启动");
            System.out.println(Thread.currentThread().getName() + "结束");
        }
    }

    public static class Runnable02 implements Runnable{

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "启动");
            System.out.println(Thread.currentThread().getName() + "结束");
        }
    }

    public static class Callable03 implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println(Thread.currentThread().getName() + "启动");
            System.out.println(Thread.currentThread().getName() + "结束");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            return 100;
        }
    }
}
