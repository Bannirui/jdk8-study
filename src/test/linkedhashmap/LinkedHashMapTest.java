package test.linkedhashmap;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *@author dingrui
 *@date 2021-01-20
 *@description
 */
public class LinkedHashMapTest {

    @Test
    public void test1() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        // put顺序
        map.put("dingrui", 2);
        map.put("lixiao", 1);
        // 输出顺序 默认的accessOrder是false 输出顺序是插入顺序
        map.entrySet().forEach(o -> {
            System.out.println(o.getKey() + "->" + o.getValue());
        });
    }

    @Test
    public void test2() {
        // 指定accessOrder是true 输出顺序是访问顺序
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        // put顺序
        map.put("dingrui", 2);
        map.put("lixiao", 1);
        map.put("nini", 3);

        // get顺序
        map.get("nini");
        map.get("dingrui");
        // 输出顺序
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
    }

    /**
     * @author dingrui
     * @date 2021/1/20
     * @return
     * @description 测试LRU
     */
    @Test
    public void test3() {
        MyLRU<Integer, Integer> myLRU = new MyLRU<Integer, Integer>(5, 0.75f);
        myLRU.put(1, 1);
        myLRU.put(2, 2);
        myLRU.put(3, 3);
        myLRU.put(4, 4);
        myLRU.put(5, 5);
        myLRU.put(6, 6);
        myLRU.put(7, 7);

        // {3=3, 4=4, 5=5, 6=6, 7=7}
        System.out.println(myLRU);

        myLRU.get(4);

        // {3=3, 5=5, 6=6, 7=7, 4=4}
        System.out.println(myLRU);

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021-01-20
     * @description 最近最少使用 设置accessOrder为true 最新访问的节点都放在tail上 那么head节点就是最老的数据 最少访问的 想办法移除head节点就行
     */
    class MyLRU<K, V> extends LinkedHashMap<K, V> {
        /**
         * 保存缓存的容量
         */
        private int capacity;

        public MyLRU(int capacity, float loadFactor) {
            super(capacity, loadFactor, true);
            this.capacity = capacity;
        }

        /**
         * 重写removeEldestEntry()方法设置何时移除旧元素
         * @param eldest
         * @return
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            // 当元素个数大于了缓存的容量, 就移除元素
            return size() > this.capacity;
        }
    }
}
