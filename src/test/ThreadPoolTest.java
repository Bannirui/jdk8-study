package test;

import org.junit.Test;

import java.util.concurrent.*;

/**
 *@author dingrui
 *@date 2021-01-05
 *@description 线程池源码学习
 */
public class ThreadPoolTest {

    /**
     * @author dingrui
     * @date 2021/1/5
     * @return
     * @description 手动创建线程池 将任务执行分配给核心线程执行
     */
    @Test
    public void test1() {
        // 手动创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                5,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                r -> {
                    System.out.println("创建线程：" + r.hashCode());
                    //线程命名
                    return new Thread(r, "threadPool" + r.hashCode());
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        // 提交一个任务
        executor.execute(() -> System.out.println("ok"));
    }

    /**
     * @author dingrui
     * @date 2021/1/5
     * @return
     * @description 手动创建线程池 将任务执行分配给非核心线程执行
     */
    @Test
    public void test5() {
        // 手动创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                0,
                4,
                3L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                r -> {
                    System.out.println("创建线程：" + r.hashCode());
                    //线程命名
                    return new Thread(r, "threadPool" + r.hashCode());
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        // 设置coreThreadTimeOut这个属性可以让核心线程在空闲的时候也死亡 也就是说没有任务空闲的时候线程池不维护任何一个线程
        executor.allowCoreThreadTimeOut(false);
        // 提交一个任务
        for (int i = 0; i < 6; i++) {
            executor.execute(() -> {
                try {
                    Thread.sleep(1000 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("ok");
            });
        }
    }

    /**
     * @author dingrui
     * @date 2021/1/5
     * @return
     * @description newSingleThreadExecutor
     * Executors指定了默认参数：
     *     corePoolSize=1
     *     maximumPoolSize=1
     *     keepAliveTime=0
     *     timeUnit=TimeUnit.MILLISECONDS
     *     taskQueue=无界队列
     * Executors调用ThreadPoolExecutor构造函数 ThreadPoolExecutor补充两个参数 一共7个参数 调用全参构造函数
     *     threadFactory=Executors.defaultThreadFactory()->new DefaultThreadFactory()
     *     handler=defaultHandler->new AbortPolicy()
     *
     */
    @Test
    public void test2() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> System.out.println("ok"));
    }

    /**
     * @author dingrui
     * @date 2021/1/5
     * @return
     * @description SecurityManager
     */
    @Test
    public void test3() {
        SecurityManager securityManager = System.getSecurityManager();
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/5
     * @return
     * @description ctl 高3位存储线程池工作状态 低29位存储工作线程数量
     */
    @Test
    public void test4() {
        // 移位操作
        int COUNT_BITS = 29;
        // -1这个32位整型的高3位表示running状态
        int running = -1 << COUNT_BITS;
        // 工作线程数量0个
        int workerCount = 0;
        // 工作线程数量上限
        int CAPACITY = (1 << COUNT_BITS) - 1;
        // 计算ctl
        int ctl = running | workerCount;
        // 从ctl中计算出线程池状态
        int runStateOf = ctl & ~CAPACITY;
        // 从ctl中计算出工作线程数量
        int workerCountOf = ctl & CAPACITY;

        System.out.println();
    }
}
