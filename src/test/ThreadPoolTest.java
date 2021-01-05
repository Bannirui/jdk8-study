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
     * @description
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
        //提交一个任务
        executor.execute(() -> System.out.println("ok"));
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
}
