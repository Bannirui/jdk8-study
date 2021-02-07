package test.cyclicbarrier;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *@author dingrui
 *@date 2021-02-07
 *@description
 */
public class CyclicBarrierTest {

    /**
     * @author dingrui
     * @date 2021/2/7
     * @param args
     * @return
     * @description 测试用例
     */
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                System.out.println("before");
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("after");
            }).start();
        }
    }
}
