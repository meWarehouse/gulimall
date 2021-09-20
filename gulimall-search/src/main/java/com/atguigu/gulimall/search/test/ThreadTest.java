package com.atguigu.gulimall.search.test;

import com.sun.xml.internal.ws.util.CompletedFuture;
import jdk.management.resource.internal.inst.SocketOutputStreamRMHooks;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author zero
 * @create 2020-09-21 12:42
 */
public class ThreadTest {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
    public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(10,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(20000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("......main.....start.......");

      /*  CompletableFuture.runAsync(() -> {
            System.out.println("当前线程-->" + Thread.currentThread().getName());
            int  i = 10 /2;
            System.out.println("运行结果："+i);

        },THREAD_POOL);*/

      /*  CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程-->" + Thread.currentThread().getName());
            int i = 10 / 0;
            System.out.println("运行结果：" + i);
            return i;
        }, THREAD_POOL).whenCompleteAsync((res,exception)->{
            System.out.println("结果是:"+res+";异常："+exception);
        }).exceptionally(throwable -> {
            System.out.println("异常是："+throwable);
            return 10;
        });
        //R apply(T t);
        //void accept(T t, U u);
        */
/*
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程-->" + Thread.currentThread().getName());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }, THREAD_POOL).handle((res, thr) -> {
            if (res != null) {
                return res * 2;
            }
            if (thr != null) {
                return 0;
            }
            return 0;
        });*/
        //R apply(T t, U u);

//        Integer integer = future.get();

        /**
         * 线程串行化方法
         * 1.thenRun 无接受值，无返回值
         *          thenRun(() -> {
         *             System.out.println("任务2 启动。。。。。");
         *          });
         *2.thenAccept 接受上一步的结果，无返回值
         *          thenAccept(res -> {
         *             System.out.println("任务2启动。。。。。");
         *             System.out.println("hello -- 2:"+res);
         *         });
         *3.thenApplyAsync 有接收值，也有返回值
         *         thenApplyAsync(res -> {
         *             System.out.println("任务2启动。。。。。，上一步的结果：" + res);
         *             int i = res * 2;
         *             System.out.println("---处理后:" + i);
         *             return i;
         *         },THREAD_POOL);
         *
         */
      /*  CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            int i = 10 / 4;
            System.out.println("运行的结果：" + i);
            return i;
        }, THREAD_POOL).thenApplyAsync(res -> {
//            try{ TimeUnit.SECONDS.sleep(3); }catch( InterruptedException e ){ e.printStackTrace(); }
            System.out.println("任务2启动。。。。。，上一步的结果：" + res);
            int i = res * 2;
            System.out.println("---处理后:" + i);
            return i;
        },THREAD_POOL);*/
        //R apply(T t);
        //void accept(T t)

        CompletableFuture<Object> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程1开始：" + Thread.currentThread().getName());
            int i = 10 / 4;
            System.out.println("线程1：结束" + i);
            return i;
        }, THREAD_POOL);


        CompletableFuture<Object> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程2开始：" + Thread.currentThread().getName());
            try{ TimeUnit.SECONDS.sleep(3); }catch( InterruptedException e ){ e.printStackTrace(); }
            System.out.println("线程2：结束" );
            return "hello";
        }, THREAD_POOL);


       /* f1.runAfterBothAsync(f2,() -> {
            System.out.println("任务3开始，，，，，，");
        },THREAD_POOL);*/

     /*  f1.thenAcceptBothAsync(f2,(res1,res2) -> {
           System.out.println("任务三开始，，，，，，");
           System.out.println("任务3结束：得到的结果："+res1+"==>"+res2);
       },THREAD_POOL);*/

        /*CompletableFuture<String> future = f1.thenCombineAsync(f2, (rest1, rest2) -> {
            System.out.println("任务三开始，，，，，，");
            System.out.println("任务3结束：得到的结果：" + rest1 + "==>" + rest2);
            return "hhah" + rest1 + rest2;
        }, THREAD_POOL);*/


//        f1.runAfterEitherAsync(f2,() -> {
//            System.out.println("任务三开始，，，，，，");
//        },THREAD_POOL);

//        f1.acceptEitherAsync(f2,re -> {
//            System.out.println("任务三开始，，，，，，:"+re);
//        },THREAD_POOL);

        CompletableFuture<String> future = f1.applyToEitherAsync(f2, o -> {
            System.out.println("任务三开始，，，，，，:" + o);
            return o + "==>oop";
        }, THREAD_POOL);


        System.out.println("......main.....end......."+future.get());

        THREAD_POOL.shutdown();

       /* Long l1 = 127L;
        Long l2 = 127L;

        System.out.println(l1 == l2);

        Double d1 = 2.0;
        Double d2 = 2.0;
        System.out.println(d1 == d2);
*/





    }

}
