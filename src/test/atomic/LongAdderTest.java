package test.atomic;

import org.junit.Test;

import java.util.concurrent.atomic.LongAdder;

/**
 *@author dingrui
 *@date 2021-02-01
 *@description
 */
public class LongAdderTest {

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        LongAdder longAdder = new LongAdder();
    }

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description add()方法
     */
    @Test
    public void test2() {
        LongAdder longAdder = new LongAdder();
        longAdder.add(1L);
    }
}
