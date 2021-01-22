package test.concurrenthashmap;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 *@author dingrui
 *@date 2021-01-22
 *@description
 */
public class ConcurrentHashMapTest {

    /**
     * @author dingrui
     * @date 2021/1/22
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    }

    /**
     * @author dingrui
     * @date 2021/1/22
     * @return
     * @description 指定初始化容量大小
     */
    @Test
    public void test2() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>(3);
    }

    @Test
    public void test3() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.computeIfAbsent("dingrui", (key) -> map.put("dingrui", 11));
    }
}
