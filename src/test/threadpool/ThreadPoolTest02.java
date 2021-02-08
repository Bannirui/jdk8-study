package test.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *@author dingrui
 *@date 2021-02-08
 *@description
 */
public class ThreadPoolTest02 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 新建一个固定5个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        List<Future<Integer>> futureList = new ArrayList<>();
        // 提交5个任务，分别返回0、1、2、3、4
        for (int i = 0; i < 5; i++) {
            int num = i;

            // 任务执行的结果用Future包装
            Future<Integer> future = threadPool.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("return: " + num);
                // 返回值
                return num;
            });

            // 把future添加到list中
            futureList.add(future);
        }

        // 任务全部提交完再从future中get返回值，并做累加
        int sum = 0;
        for (Future<Integer> future : futureList) {
            sum += future.get();
        }

        System.out.println("sum=" + sum);
    }
}

