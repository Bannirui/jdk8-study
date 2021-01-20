package test.concurrent.waitnotify;

import org.junit.Test;
import test.concurrent.waitnotify.DecreaseThread;
import test.concurrent.waitnotify.IncreaseAndDecrease;
import test.concurrent.waitnotify.IncreaseThread;

/**
 *@author dingrui
 *@date 2021-01-19
 *@description
 * wait：
 *     1，当调用对象的wait方法时，必须确保调用wait方法的这个线程已经持有了这个对象的monitor监视器锁
 *     2，当调用对象的wait方法后，该线程就会释放掉该对象的监视器锁，然后进入等待状态，进入了monitor的wait set
 *     3，当线程调用wait后进入等待状态时，它就可以等待其他线程调用相同对象的notify或者notifyAll方法使自己被唤醒
 *     4，一旦这个线程被其他线程唤醒后，该线程就会与其他线程一起开始竞争这个对象的锁，公平竞争，只有当该线程获取到这个对象的锁之后，线程才会继续往下执行
 *     5，调用Thread的sleep方法时，线程并不会释放掉对象的锁
 *     6，调用wait方法的代码片段需要放在一个synchronized块或者synchronized方法中，这样才可以保证线程在调用wait方法之前已经获取到了对象的锁
 *
 * notify：
 *     1，当调用对象的notify方法使，它会随机唤醒这个对象等待集合wait set中的任意一个线程，当某个线程被唤醒之后，它就会与其他线程公平竞争对象的锁
 *     2，当调用对象的notifyAll方法时，它会唤醒该对象等待集合wait set中的所有线程，这些线程被唤醒之后，又会和其他线程一起公平竞争对象的锁
 *     3，在某一个时刻，只有一个线程可以拥有对象的锁
 */
public class WaitNotifyTest {

    /***
     * @author dingrui
     * @date 2021/1/19
     * @return
     * @description Object根类的wait notify方法
     * 一个线程没有持有对象的monitor监视器锁 会抛出异常IllegalMonitorStateException
     */
    @Test
    public void test1() throws InterruptedException {
        Object o = new Object();
        o.wait();
    }

    /**
     * @author dingrui
     * @date 2021/1/20
     * @return
     * @description 通过synchronized关键字获取对象的监视器锁 然后调用该对象的wait方法
     */
    @Test
    public void test2() throws InterruptedException {
        Object o = new Object();
        synchronized (o) {
            o.wait();
        }
    }


    /**
     * @author dingrui
     * @date 2021/1/20
     * @param args
     * @return
     * @description 测试wait和notify
     */
    public static void main(String[] args) {
        IncreaseAndDecrease increaseAndDecrease  = new IncreaseAndDecrease();

        IncreaseThread increaseThread = new IncreaseThread(increaseAndDecrease);
        DecreaseThread decreaseThread = new DecreaseThread(increaseAndDecrease);

        increaseThread.start();
        decreaseThread.start();
    }
}
