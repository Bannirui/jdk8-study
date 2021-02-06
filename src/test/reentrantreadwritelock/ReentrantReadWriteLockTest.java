package test.reentrantreadwritelock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *@author dingrui
 *@date 2021-02-05
 *@description
 */
public class ReentrantReadWriteLockTest {

    /**
     * @author dingrui
     * @date 2021/2/5
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    }

    /**
     * @author dingrui
     * @date 2021/2/5
     * @return
     * @description 非公平锁 读锁 加锁
     */
    @Test
    public void test2() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        readLock.lock();
    }
}
