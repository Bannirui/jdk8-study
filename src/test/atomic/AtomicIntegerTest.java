package test.atomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *@author dingrui
 *@date 2021-01-05
 *@description
 */
public class AtomicIntegerTest {

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1(){
        AtomicInteger atomicInteger = new AtomicInteger();
        System.out.println();
    }

    @Test
    public void test2(){
        AtomicInteger atomicInteger = new AtomicInteger(3);
        atomicInteger.get();
        int i = atomicInteger.getAndIncrement();
        int j = atomicInteger.get();
        System.out.println();
    }
}
