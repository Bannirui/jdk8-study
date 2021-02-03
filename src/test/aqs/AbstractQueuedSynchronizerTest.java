package test.aqs;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import org.junit.Test;

import javax.xml.stream.FactoryConfigurationError;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.stream.IntStream;

/**
 *@author dingrui
 *@date 2021-02-03
 *@description
 */
public class AbstractQueuedSynchronizerTest {

    /**
     * @author dingrui
     * @date 2021-02-03
     * @description 基于AQS实现锁
     */
    private static class MySync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int arg) {
            // 将state从0改为1 上锁
            if (compareAndSetState(0, 1)) {
                // 设置为当前线程独占
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            // 上锁失败
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // 移除当前线程独占
            setExclusiveOwnerThread(null);
            // 去锁 将state从1改成0
            setState(0);
            return true;
        }
    }

    // 声明同步器
    private final MySync mySync = new MySync();

    /**
     * 上锁
     */
    public void lock() {
        mySync.tryAcquire(1);
    }

    /**
     * 去锁
     */
    public void unlock() {
        mySync.tryRelease(1);
    }

    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        AbstractQueuedSynchronizerTest lock = new AbstractQueuedSynchronizerTest();

        CountDownLatch countDownLatch = new CountDownLatch(20);

        IntStream.range(0, 1000).forEach(i -> new Thread(() -> {
            lock.lock();
            try {
                IntStream.range(0, 10000).forEach(j -> {
                    count++;
                });
            } finally {
                lock.unlock();
            }
            countDownLatch.countDown();
        }, "tt-" + i).start());

        countDownLatch.await();

        System.out.println(count);
    }
}
