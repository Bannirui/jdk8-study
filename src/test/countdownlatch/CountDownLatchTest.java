package test.countdownlatch;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 *@author dingrui
 *@date 2021-02-07
 *@description
 */
public class CountDownLatchTest {

    /**
     * @author dingrui
     * @date 2021/2/7
     * @param args
     * @return
     * @description 使用案例
     */
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    System.out.println("Aid thread is waiting for starting.");
                    startSignal.await();
                    // do sth
                    System.out.println("Aid thread is doing something.");
                    doneSignal.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // main thread do sth
        Thread.sleep(2000);
        System.out.println("Main thread is doing something.");
        startSignal.countDown();

        // main thread do sth else
        System.out.println("Main thread is waiting for aid threads finishing.");
        doneSignal.await();

        System.out.println("Main thread is doing something after all threads have finished.");
    }

    /**
     * @author dingrui
     * @date 2021/2/7
     * @return
     * @description 构造方法
     */
    @Test
    public void test1() {
        CountDownLatch latch = new CountDownLatch(5);
        System.out.println();
    }
}
