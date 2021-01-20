package test.concurrent.synchronize;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *@author dingrui
 *@date 2021-01-20
 *@description
 */
public class SynchronizedTest1 {

    @Test
    public void test1() {
        Runnable myThread = new MyThread();
        Thread t1 = new Thread(myThread);
        Thread t2 = new Thread(myThread);

        t1.start();
        t2.start();
    }
}

class MyThread implements Runnable {

    int x;

    @Override
    public void run() {
        x = 0;
        while (true) {
            System.out.println("Result:" + x++);
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (x == 30) {
                break;
            }
        }
    }
}
