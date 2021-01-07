package test;

import org.junit.Test;

import java.util.HashMap;

/**
 * @author dingrui
 * @create 2020-09-02
 * @Description
 */
public class HashMapTest {

    /**
     * @author dingrui
     * @date 2021/1/7
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        // 无参构造函数只做了一件事情 给loadFactor赋上默认值0.75
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.get("key-1");
        hashMap.put("key-1", "value-1");
        hashMap.put("key-2", "value-2");
        hashMap.put("key-3", "value-3");
        hashMap.put("key-4", "value-4");
        hashMap.put("key-5", "value-5");
        hashMap.put("key-6", "value-6");
        hashMap.put("key-7", "value-7");
        hashMap.put("key-8", "value-8");
        hashMap.put("key-9", "value-9");
        hashMap.put("key-10", "value-10");
        hashMap.put("key-11", "value-11");
        hashMap.put("key-12", "value-12");
        hashMap.put("key-13", "value-13");
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/7
     * @return
     * @description 有参构造方法 指定初始化容量
     */
    @Test
    public void test2() {
        HashMap<String, String> hashMap = new HashMap<>(17);
        hashMap.get("key-1");
        hashMap.put("key-1", "value-1");
        System.out.println();
    }

    @Test
    public void test3() {
        HashMap<String, String> map = new HashMap<>(3);
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("k4", "v4"); // 指定初始化容量是3 第一次put初始化出来容量是4 阈值是3 这次put就会触发扩容 只要让前面3次put的key的hash值一样就可以模拟
        map.put("k5", "v5");

        System.out.println();
    }
}
