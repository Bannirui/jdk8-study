package test.priorityqueue;

import org.junit.Test;

import java.util.PriorityQueue;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class PriorityQueueTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description offer测试
     */
    @Test
    public void test1() {
        PriorityQueue<String> queue = new PriorityQueue<>();
        queue.add("A");
        queue.add("C");
        queue.add("B");
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 扩容
     */
    @Test
    public void test2() {
        PriorityQueue<String> queue = new PriorityQueue<>(1);
        queue.add("ding1");
        // 初始化队列的容量只有1 再增加元素的时候就已经满了触发扩容
        queue.add("ding2");
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description poll弹出测试
     */
    @Test
    public void test3() {
        PriorityQueue<String> queue = new PriorityQueue<>();
        queue.add("D");
        queue.add("A");
        queue.add("B");
        queue.add("C");

        String poll = queue.poll();

        System.out.println();
    }
}
