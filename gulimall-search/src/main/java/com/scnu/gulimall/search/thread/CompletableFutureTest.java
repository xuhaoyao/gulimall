package com.scnu.gulimall.search.thread;

import java.util.Date;
import java.util.concurrent.*;

public class CompletableFutureTest {

    private static ExecutorService pool;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println(Thread.currentThread().getName() + "启动");

        //test01(); //whenComplete
        //test02(); //handle
        //test03(); //线程串行化方法
        //test04(); //两任务组合,都要完成
        //test05();   //两任务组合,一个完成
        System.out.println("开始时间:" + new Date());
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("查询商品Img");
            return "图片111";
        }, pool);
        CompletableFuture<String> futurePrice = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品价格");
            return "价格111";
        }, pool);
        CompletableFuture<String> futureStock = CompletableFuture.supplyAsync(() -> {
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("查询商品Stock");
            return "Stock111";
        }, pool);
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futurePrice, futureStock);
        allOf.get();
        System.out.println(futureImg.get() + "-->" + futurePrice.get() + "-->" + futureStock.get());
        System.out.println(Thread.currentThread().getName() + "结束");
        System.out.println("结束时间:" + new Date());
    }

    /**
     * whenCompleteAsync ->BiConsumer<? super T, ? super Throwable> action
     *                  void accept(T t, U u);
     * exceptionally ->Function<Throwable, ? extends T> fn
     *                  R apply(T t);
     * whenComplete 可以处理正常和异常的计算结果，exceptionally 处理异常情况。
     * whenComplete 和 whenCompleteAsync 的区别：
     * whenComplete：是执行当前任务的线程执行继续执行 whenComplete 的任务。
     * whenCompleteAsync：是执行把 whenCompleteAsync 这个任务继续提交给线程池
     * 来进行执行。
     * 方法不以 Async 结尾，意味着 Action 使用相同的线程执行，而 Async 可能会使用其他线程
     * 执行（如果是使用相同的线程池，也可能会被同一个线程选中执行）
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void test01() throws ExecutionException, InterruptedException {
        //        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
//            System.out.println(Thread.currentThread().getName() + "启动");
//            System.out.println(Thread.currentThread().getName() + "结束");
//        },pool);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "启动");
            int a = 10 / 0;
            System.out.println(Thread.currentThread().getName() + "结束");
            return "hello";
        },pool).whenCompleteAsync((res,e) -> {
            //能感知异常但是没有返回值
            System.out.println("whenComplete--->异步任务完成了,结果是:" + res + "异常是:" + e);
        }).exceptionally(e -> {
            //出现异常的回调方法,可以返回值
            System.out.println("exceptionally--->出现异常:" + e);
            return "bye";
        });

        System.out.println("CompletableFuture<String>返回结果:" + future2.get());
    }

    /**
     * handle -> BiFunction<? super T, Throwable, ? extends U> fn
     *              R apply(T t, U u);
     * 和 complete 一样，可对结果做最后的处理（可处理异常），可改变返回值。
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void test02() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "启动");
            int a = 10 / 0;
            System.out.println(Thread.currentThread().getName() + "结束");
            return "hello";
        },pool).handle((res, e) -> {
            if (res != null) {
                return res.toUpperCase();
            }
            if (e != null) {
                return "error:" + e;
            }
            return null;
        });
        System.out.println("CompletableFuture<String>返回结果:" + future.get());
    }


    /**
     * thenApplyAsync:接受前者返回的执行结果,并可以返回一个值
     * thenAcceptAsync:接受前者返回的执行结果,但是不能返回值
     * thenRunAsync:  前者执行完成之后执行此方法,不接受前者的执行结果,也不返回值
     *
     * 方法不以 Async 结尾，意味着 Action 使用相同的线程执行，而 Async 可能会使用其他线程
     * 执行（如果是使用相同的线程池，也可能会被同一个线程选中执行）
     */
    public static void test03(){
        CompletableFuture.supplyAsync(()->{
            System.out.println(Thread.currentThread().getName() + "启动");
            System.out.println(Thread.currentThread().getName() + "结束");
            return "VarerLeet";
        },pool).thenApplyAsync(res -> {
            System.out.println("thenApplyAsync->上个任务执行完后,将结果:" + res + "传入,然后可以继续执行操作,再返回一个值");
            return res + " pong";
        },pool).thenAcceptAsync(res -> {
            System.out.println("thenAcceptAsync->上个任务执行完后,将返回结果传入->" + res);
        }).thenRunAsync(() -> {
            System.out.println("thenRunAsync->上个任务执行完后执行");
        },pool);
    }

    /**
     * 两任务组合,都要完成
     */
    public static void test04(){
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "执行");
            return "任务1";
        }, pool);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "执行");
            return "任务2";
        }, pool);
//        future1.runAfterBoth(future2,()->{
//            System.out.println("任务1和任务2执行完之后,执行runAfterBoth,无参数,无返回结果");
//        },pool);
//        future1.thenAcceptBothAsync(future2,(res1,res2)->{
//            System.out.println("任务1和任务2执行完之后,执行thenAcceptBothAsync," +
//                    "无返回结果,当可以接受它们两个的返回值:" + res1 + "--->" + res2);
//        },pool);
        future1.thenCombineAsync(future2,(res1,res2)->{
            System.out.println("thenCombineAsync接受任务1和任务2的返回值,同时可以返回一个结果");
            return res1 + res2;
        },pool).thenAcceptAsync(System.out::println,pool);
    }

    /**
     * 两任务组合  一个完成
     * applyToEither：两个任务有一个执行完成，获取它的返回值，处理任务并有新的返回值。
     * acceptEither：两个任务有一个执行完成，获取它的返回值，处理任务，没有新的返回值。
     * runAfterEither：两个任务有一个执行完成，不需要获取 future 的结果，处理任务，也没有返回值。
     */
    public static void test05(){
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "开始");
            System.out.println(Thread.currentThread().getName() + "结束");
            return "任务1";
        }, pool);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "开始");
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName() + "结束");
            return "任务2";
        }, pool);

//        future1.runAfterEitherAsync(future2,()->{
//            System.out.println("两个任务其中一个执行完后就可以执行,没有参数,没有返回值");
//        },pool);
//        future1.acceptEitherAsync(future2,res -> {
//            System.out.println("两个任务其中一个执行完后就可以执行,接受到其中一个任务的结果:" + res + ",没有返回值");
//        },pool);
        future1.applyToEitherAsync(future2,res -> {
            System.out.println("两个任务其中一个执行完后就可以执行,接受一个值,可以返回结果:" + res);
            return res + res;
        },pool).thenAcceptAsync(System.out::println,pool);
    }

    static {
        pool = new ThreadPoolExecutor(5,10,100, TimeUnit.SECONDS,new LinkedBlockingQueue<>(10));
    }

}
