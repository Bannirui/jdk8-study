package test.hashmap;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 *@author dingrui
 *@date 2021-01-08
 *@description
 */
public class ClassTest {

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description 验证HashMap#comparableClassFor#x.getClass这个方法
     * 1，getClass获取的是具类，不管对象怎么转型获取的都是运行时类型，也就是new的时候的类型
     * 2，匿名对象，匿名对象调用getClass()时返回的是依赖它的对象的运行时类型，并以1,2,3…的索引区分
     */
    @Test
    public void test1() {
        D d = new D();

        // class test.hashmap.ClassTest$1
        System.out.println(new A() {
        }.getClass());

        // class test.hashmap.ClassTest$2
        System.out.println(new B() {
        }.getClass());

        // class test.hashmap.ClassTest$3
        System.out.println(new Comparable<Object>() {
            @Override
            public int compareTo(Object o) {
                return 0;
            }
        }.getClass());

        // class test.hashmap.ClassTest$D$1
        System.out.println(d.c.getClass());

        // class test.hashmap.ClassTest$E
        E e = new E();
        C e1 = e;
        System.out.println(e1.getClass());
    }


    abstract class A {
    }

    abstract class B {
    }

    abstract class C {
    }

    class D {
        C c;

        D() {
            c = new C() {
            };
        }
    }

    class E extends C {
    }

    // ------------------------------------ 手动分割 ------------------------------------

    abstract class Grand implements Comparable<Grand> {
    }

    abstract class Super extends Grand implements Serializable {
    }

    class Child extends Super implements Cloneable {
        public int compareTo(Grand o) {
            return 0;
        }
    }

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description
     * getGenericInterfaces()方法返回的是该对象的运行时类型“直接实现”的接口，这意味着
     * 1，返回的一定是接口
     * 2，必然是该类型自己实现的接口，继承过来的不算
     */
    @Test
    public void test2() {
        Child child = new Child();
        Type[] types = child.getClass().getGenericInterfaces();
        if (types != null) {
            for (Type type : types) {
                // java.lang.Cloneable
                System.out.println(type.getTypeName());
            }
        }
    }

    // ------------------------------------ 手动分割 ------------------------------------

    abstract class Grand1 implements Comparable<Grand1> {
    }

    abstract class Super1<T, E> extends Grand1 implements Serializable {
    }

    class Child1 extends Super1<Integer, Integer> implements Cloneable {
        @Override
        public int compareTo(Grand1 o) {
            return 0;
        }
    }

    class Child2<A, B, C> extends Super1<A, B> {
        @Override
        public int compareTo(Grand1 o) {
            return 0;
        }
    }

    // class Child3 extends Super1 {
    //     @Override
    //     public int compareTo(Grand1 o) {
    //         return 0;
    //     }
    // }

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description
     * 1，getSuperclass()返回的是直接父类的类型，不包括泛型参数
     * 2，getGenericSuperclass()返回的是包括泛型参数在内的直接父类
     * 3，注意如果父类声明了泛型，但子类继承时没有为父类实现该泛型，这时候也是没有泛型参数的
     */
    @Test
    public void test3() {

        String typeName;
        String name;

        Grand1 child1 = new Child1();
        Type type1 = child1.getClass().getGenericSuperclass();
        Class<?> class1 = child1.getClass().getSuperclass();
        //
        typeName = type1.getTypeName();
        //
        name = class1.getTypeName();

        Grand1 child2 = new Child2();
        Type type2 = child2.getClass().getGenericSuperclass();
        Class<?> class2 = child2.getClass().getSuperclass();
        //
        typeName = type2.getTypeName();
        //
        name = class2.getTypeName();

        // Grand1 child3 = new Child3();
        // Type type3 = child3.getClass().getGenericSuperclass();
        // Class<?> class3 = child3.getClass().getSuperclass();
        //
        // typeName = type3.getTypeName();
        //
        // name = class3.getTypeName();


        System.out.println();
    }
}
