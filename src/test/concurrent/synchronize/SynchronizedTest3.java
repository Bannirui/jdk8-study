package test.concurrent.synchronize;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-01-25
 *@description
 */
public class SynchronizedTest3 {

    private Object object = new Object();

    public void method1() {
        synchronized (object) {
            System.out.println("hello1");
        }
    }

    public void method2() {
        synchronized (object) {
            System.out.println("hello2");
            throw new RuntimeException();
        }
    }

    public synchronized void method3() {
        System.out.println("hello3");
    }

    public static synchronized void method4() {
        System.out.println("hello4");
    }

    @Test
    public void test1() {
        new SynchronizedTest3().method1();
    }
}
