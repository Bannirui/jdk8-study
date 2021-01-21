package test.weakhashmap;

import org.junit.Test;

import java.util.Map;
import java.util.WeakHashMap;

/**
 *@author dingrui
 *@date 2021-01-21
 *@description
 */
public class WeakHashMapTest {

    @Test
    public void test1() {
        WeakHashMap<String, Integer> map = new WeakHashMap<>();

    }

    @Test
    public void test2() {
        Map<String, Integer> map = new WeakHashMap<>(3);

        // 放入3个new String()声明的字符串 new String()声明的变量才是弱引用
        map.put(new String("1"), 1);
        map.put(new String("2"), 2);
        map.put(new String("3"), 3);

        // 放入不用new String()声明的字符串 使用"6"这种声明方式会一直存在于常量池中，不会被清理
        map.put("6", 6);

        // 使用key强引用"3"这个字符串
        String key = null;
        for (String s : map.keySet()) {
            // 这个"3"和new String("3")不是一个引用
            if (s.equals("3")) {
                key = s;
            }
        }

        // 输出{6=6, 1=1, 2=2, 3=3}，未gc所有key都可以打印出来
        System.out.println(map);

        // gc一下
        System.gc();

        // {6=6, 3=3}
        System.out.println(map);

        // 放一个new String()声明的字符串
        map.put(new String("4"), 4);

        // 输出{4=4, 6=6, 3=3}，gc后放入的值和强引用的key可以打印出来
        System.out.println(map);

        // key与"3"的引用断裂
        key = null;

        // gc一下
        System.gc();

        // 输出{6=6}，gc后强引用的key可以打印出来
        System.out.println(map);
    }
}
