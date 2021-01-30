package test.concurrentlinkedqueue;

import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *@author dingrui
 *@date 2021-01-30
 *@description
 */
public class ConcurrentLinkedQueueTest {

    /**
     * @author dingrui
     * @date 2021/1/30
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    }
}
