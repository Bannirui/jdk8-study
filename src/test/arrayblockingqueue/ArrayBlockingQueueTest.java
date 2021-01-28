package test.arrayblockingqueue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class ArrayBlockingQueueTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    }

    @Test
    public void test2() throws InterruptedException {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        queue.put("ding1");
        queue.offer("ding2");
        queue.add("ding3");
        // 到这为止 队列就已经满了 putIndex=2 tabkeIndex=0
        queue.take();
        queue.poll();
        queue.take();
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 构造方法传集合进去
     */
    @Test
    public void test3() {
        // list中放2个元素
        ArrayList<String> list = new ArrayList<>();
        list.add("ding1");
        list.add("ding2");
        // new一个容量是1的queue 但是放的集合中有两个元素 初始化的时候逐个放元素 queue会满 触发异常抛出来
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1, true, list);
    }
}
