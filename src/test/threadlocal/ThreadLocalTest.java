package test.threadlocal;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-12
 *@description
 */
public class ThreadLocalTest {

    @Test
    public void test1() {
        ThreadLocal<Long> tl = new ThreadLocal<>();
        tl.set(1L);
        Long value = tl.get();
        tl.remove();
        System.out.println();
    }

    @Test
    public void test2() {
        ThreadLocal<Long> tl1 = new ThreadLocal<>();
        ThreadLocal<Long> tl2 = new ThreadLocal<>();
        System.out.println();
    }
}
