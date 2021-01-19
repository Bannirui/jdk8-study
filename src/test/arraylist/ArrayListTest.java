package test.arraylist;

import org.junit.Test;

import java.util.ArrayList;

/**
 *@author dingrui
 *@date 2021-01-08
 *@description
 */
public class ArrayListTest {

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description 无参构造方法
     * 无参构造方法new出来的列表 此刻底层是个空数组 长度是0 数组里面没有元素
     */
    @Test
    public void test1() {
        ArrayList<Integer> list = new ArrayList<>();
        int size = list.size();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);
        list.add(10);

        System.out.println();
    }


    @Test
    public void test3() {
        ArrayList<Integer> list = new ArrayList<>(0);
    }

    @Test
    public void test4() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);

        Integer res = list.get(-1);

        System.out.println();
    }

    @Test
    public void test5() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(1);
        list.add(1);

        list.remove(null);
        list.remove(1);

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/19
     * @return
     * @description 求两个集合的交集
     */
    @Test
    public void test6() {
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();

        list1.add(1);
        list1.add(2);
        list1.add(3);

        list2.add(3);
        list2.add(4);
        list2.add(5);

        list1.retainAll(list2);

        System.out.println();
    }

    @Test
    public void test7() {
        int[] arr = {2, 3, 3};
        int size = arr.length;
        int r = 2;
        int w = 1;
        System.arraycopy(arr, r, arr, w, size - r);

        System.out.println();
    }

    @Test
    public void test8() {
        int i = 0;
        for (; i < 3; i++) {
            System.out.println(i);
        }

        System.out.println();
    }
}
