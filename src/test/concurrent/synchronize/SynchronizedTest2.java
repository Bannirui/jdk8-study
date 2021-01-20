package test.concurrent.synchronize;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-01-20
 *@description
 * synchronized:
 *     1，一个对象有多个synchronized修饰的方法 必须要等该对象的monitor锁释放才能执行另一个方法
 *     2，synchronized修饰的static方法 本质上不是对象私有的方法 锁对象是该对象对应的class对象 不管对象有多少个 对象对应的class对象只有一个
 *     3，锁释放：
 *         正常执行结束
 *         执行抛出异常
 */
public class SynchronizedTest2 {

    public static void main(String[] args) throws InterruptedException {
        MyClass myClass = new MyClass();
        MyClass myClass2 = new MyClass();

        MyThread1 t1 = new MyThread1(myClass);
        // MyThread2 t2 = new MyThread2(myClass);
        MyThread2 t2 = new MyThread2(myClass2);

        t1.start();
        Thread.sleep(1000);
        t2.start();
    }

}

class MyClass {

    public synchronized void hello() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("hello");
    }

    public synchronized void world() {
        System.out.println("world");
    }
}

class MyThread1 extends Thread{

    private MyClass myClass;

    public MyThread1(MyClass myClass) {
        this.myClass=myClass;
    }

    @Override
    public void run() {
        myClass.hello();
    }
}

class MyThread2 extends Thread{

    private MyClass myClass;

    public MyThread2(MyClass myClass) {
        this.myClass=myClass;
    }

    @Override
    public void run() {
        myClass.world();
    }
}
