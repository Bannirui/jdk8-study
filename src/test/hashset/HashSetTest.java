package test.hashset;

import org.junit.Test;

import java.util.HashSet;

/**
 *@author dingrui
 *@date 2021-01-27
 *@description HashSet学习
 */
public class HashSetTest {

    /**
     * @author dingrui
     * @date 2021/1/27
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        HashSet<String> set = new HashSet<>();
    }

    @Test
    public void test2() {
        HashSet<Integer> set = new HashSet<>();
        set.add(1);
        set.add(1);
        System.out.println();
    }

    @Test
    public void test3() {
        HashSet<Integer> set = new HashSet<>();
        set.add(null);
        System.out.println();
    }
}
