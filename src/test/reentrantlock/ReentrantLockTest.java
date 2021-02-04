package test.reentrantlock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 *@author dingrui
 *@date 2021-02-03
 *@description
 */
public class ReentrantLockTest {

    /**
     * @author dingrui
     * @date 2021/2/3
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ReentrantLock lock = new ReentrantLock();
    }
}
