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

    @Test
    public void test3() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.computeIfAbsent("dingrui", (key) -> map.put("dingrui", 11));
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
        hm.put("ding1",1);
        hm.put("ding2",2);
        hm.put("ding3",3);

        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        chm.putAll(hm);
    }
}
