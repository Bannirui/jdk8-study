package test.atomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *@author dingrui
 *@date 2021-01-05
 *@description
 */
public class AtomicIntegerTest {

    @Test
    public void test1(){
        AtomicInteger atomicInteger = new AtomicInteger();
        int i = atomicInteger.get();
        atomicInteger.set(2);
        int j = atomicInteger.get();
        int l = atomicInteger.getAndIncrement();

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
