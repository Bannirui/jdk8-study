package test.concurrenthashmap;

import org.junit.Test;

import java.util.HashMap;
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

    /**
     * @author dingrui
     * @date 2021/3/16
     * @return
     * @description jdk8中concurrentHashMap的bug
     *
     */
    @Test
    public void test3() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        String key = "dingrui";
        map.computeIfAbsent(key, (k) -> map.put(k, 11));
    }

    @Test
    public void test5() {
        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        String key = "dingrui";
        Integer value = chm.put(key, 12);
        chm.computeIfAbsent(key, (k) -> 11);
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/25
     * @return
     * @description transfer方法太复杂了 通过putAll调用到这个方法
     */
    @Test
    public void test4() {
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("ding1", 1);
        hm.put("ding2", 2);
        hm.put("ding3", 3);

        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        chm.putAll(hm);
    }
}
