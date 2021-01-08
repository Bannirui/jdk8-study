package test;

import org.junit.Test;
import org.w3c.dom.Node;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        map.put("1", "v1");
        map.put("5", "v2");
        map.put("9", "v3");
        // 指定初始化容量是3 第一次put初始化出来容量是4 阈值是3 这次put就会触发扩容 只要让前面3次put的key的hash值一样就可以模拟
        map.put("12", "v4");
        map.put("2", "v5");

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/7
     * @return
     * @description 模仿hashMap扩容中链表迁移
     */
    @Test
    public void test4() {
        // 假使1-5的一条链表 按照奇偶性分成两条链表
        Node node = new Node(1, new Node(2, new Node(3, new Node(4, new Node(5)))));

        Node loHead = null, loTail = null;
        Node hiHead = null, hiTail = null;
        Node next;
        do {
            next = node.next;
            if (node.value % 2 == 0) {
                if (hiTail == null) {
                    hiHead = node;
                } else {
                    hiTail.next = node;
                }
                hiTail = node;
            } else {
                if (loTail == null) {
                    loHead = node;
                } else {
                    loTail.next = node;
                }
                loTail = node;
            }
        } while ((node = next) != null);

        System.out.println();
    }

    static class Node {
        int value;
        Node next;

        public Node(int value) {
            this.value = value;
        }

        public Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    /**
     * @author dingrui
     * @date 2021/1/7
     * @return
     * @description 多线程场景下使用
     */
    @Test
    public void test5() {
        Map<String, String> map = Collections.synchronizedMap(new HashMap<String, String>());
        map.put("key1", "value1");

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description for循环验证hashMap树化的边界条件
     */
    @Test
    public void test6() {
        for (int i = 0; i < 3; i++) {
            System.out.println("i:" + i);
        }

        for (int j = 0; j < 3; ++j) {
            System.out.println("j:" + j);
        }

        for (int k = 0; ; ++k) {
            if (k >= (8 - 1)) {
                System.out.println("k:" + k);
                break;
            }
        }
    }

    static class A {
    }

    static class B implements Comparable<Object> {
        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    static class C implements Comparable<C> {
        @Override
        public int compareTo(C o) {
            return 0;
        }

    }

    static class D implements Comparable<E> {
        @Override
        public int compareTo(E o) {
            return 0;
        }
    }

    static class E {
    }

    static class F extends C {
    }

    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks 1，getClass获取的是具类，不管对象怎么转型获取的都是运行时类型，也就是new的时候的类型 2，匿名对象，匿名对象调用getClass()时返回的是依赖它的对象的运行时类型，并以1,2,3…的索引区分
                return c;
            if ((ts = c.getGenericInterfaces()) != null) { // getGenericInterfaces()方法返回的是该对象的运行时类型“直接实现”的接口 1，返回的一定是接口 2，必然是该类型自己实现的接口，继承过来的不算
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                            ((p = (ParameterizedType)t).getRawType() ==
                                    Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description comparableClassFor这个方法测试
     * 只有当一个类实现了Comparable并且compareTo的比较泛型也是该类本身才会返回这个类 否则返回的是null
     */
    @Test
    public void test7() {
        Class<?> aClass = comparableClassFor(new A());
        Class<?> bClass = comparableClassFor(new B());
        Class<?> cClass = comparableClassFor(new C());
        Class<?> dClass = comparableClassFor(new D());
        Class<?> eClass = comparableClassFor(new E());
        Class<?> fClass = comparableClassFor(new F());

        System.out.println();
    }
}
