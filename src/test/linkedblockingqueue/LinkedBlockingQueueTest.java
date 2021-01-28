package test.linkedblockingqueue;

import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class LinkedBlockingQueueTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 单链表的头节点
     */
    @Test
    public void test2() {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        queue.add("ding1");
        queue.add("ding2");
        queue.add("ding3");
    }

    @Test
    public void test3() {
        AtomicInteger count = new AtomicInteger();
        int i = count.getAndIncrement();
        int i1 = count.incrementAndGet();
        System.out.println();
    }
}
