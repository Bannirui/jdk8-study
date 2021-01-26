package test.concurrent.synchronize;

import com.sun.media.sound.SoftTuning;

/**
 *@author dingrui
 *@date 2021-01-26
 *@description 死锁
 */
public class SynchronizedTest5 {

    private Object lock1 = new Object();

    private Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {
            synchronized (lock2) {
                System.out.println("method1");
            }
        }
    }

    public void method2() {
        synchronized (lock2) {
            synchronized (lock1) {
                System.out.println("method2");
            }
        }
    }

    public static void main(String[] args) {
        SynchronizedTest5 synchronizedTest5 = new SynchronizedTest5();
        Runnable runnable1 = () -> {
            while (true) {
                synchronizedTest5.method1();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread myThread1 = new Thread(runnable1, "myThread1");

        Runnable runnable2 = () -> {
            while (true) {
                synchronizedTest5.method2();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread myThread2 = new Thread(runnable2, "myThread2");

        myThread1.start();
        myThread2.start();
    }
}
