package test.copyonwritearrayset;

import org.junit.Test;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 *@author dingrui
 *@date 2021-01-28
 *@description
 */
public class CopyOnWriteArraySetTest {

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description 往set中添加重复元素 这里CopyOnWriteArraySet调用的是CopyOnWriteArrayList#addIfAbsent()方法 真正添加元素进数组前会通过index()方法校验是否在数组中已经存在该元素 如果已经存在就返回false 如果没有存在该元素 才进行真正的添加操作
     */
    @Test
    public void test2() {
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        set.add("ding1");
        set.add("ding1");
    }

    /**
     * @author dingrui
     * @date 2021/1/28
     * @return
     * @description equals()方法
     */
    @Test
    public void test3() {
        CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();
        boolean b = set.equals(set);

    }
}
