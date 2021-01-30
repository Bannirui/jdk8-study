package test.synchronousqueue;

import org.junit.Test;

import java.util.concurrent.SynchronousQueue;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class SynchronousQueueTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        SynchronousQueue<String> queue = new SynchronousQueue<>();
    }

    /**
     * @author dingrui
     * @date 2021/1/29
     * @return
     * @description put元素
     */
    @Test
    public void test2() throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();
        queue.put("ding1");
        // put完一个元素之后会等待被匹配到 阻塞在自旋里面出不来
        // 连take都走不到 必须要第二个线程进来take
        queue.take();
        queue.put("ding2");
        queue.put("ding3");
    }


    /**
     * @author dingrui
     * @date 2021/1/29
     * @param args
     * @return
     * @description 生产者-消费者 模型
     */
    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();

        // 生产者
        new Thread(() -> {
            try {
                queue.put("ding1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);

        // 主线程put
        // queue.put("ding2");

        // 消费者
        String take = queue.take();
        System.out.println("take=" + take);

        // 再生产 然后阻塞、
        // queue.put("ding2");
    }

    /**
     * @author dingrui
     * @date 2021/1/29
     * @return
     * @description 模拟取不到数据 线程被中断抛出异常
     */
    @Test
    public void test3() throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();
        String take = queue.take();
    }

    /**
     * @author dingrui
     * @date 2021/1/29
     * @return
     * @description 首次put
     */
    @Test
    public void test4() throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();
        queue.put("ding1");
    }

}
