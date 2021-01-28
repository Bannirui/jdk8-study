package test.concurrentskiplistset;

import org.junit.Test;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class ConcurrentSkipListSetTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();
    }
}
