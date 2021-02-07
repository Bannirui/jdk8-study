package test.semaphore;

import org.junit.Test;

import java.util.concurrent.Semaphore;

/**
 *@author dingrui
 *@date 2021-02-05
 *@description
 */
public class SemaphoreTest {

    /**
     * @author dingrui
     * @date 2021/2/5
     * @return
     * @description 构造方法
     */
    @Test
    public void test1() {
        Semaphore semaphore = new Semaphore(2);
    }
}
