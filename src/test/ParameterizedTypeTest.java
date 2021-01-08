package test;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;

/**
 *@author dingrui
 *@date 2021-01-08
 *@description
 */
public class ParameterizedTypeTest {

    class Grand {
    }

    class Super<A, B> extends Grand {
    }

    class Child extends Super<String, String> {
    }

    class Child2<A, B> extends Super<A, B> {
    }

    class Child3<A, B> extends Super {
    }

    /**
     * @author dingrui
     * @date 2021/1/8
     * @return
     * @description ParameterizedType是Type接口的子接口，表示参数化的类型，即实现了泛型参数的类型
     * 1，如果直接用bean对象instanceof ParameterizedType，结果都是false
     * 2，Class对象不能instanceof ParameterizedType，编译会报错
     * 3，只有用Type对象instanceof ParameterizedType才能得到想要的比较结果。可以这么理解：一个Bean类不会是ParameterizedType，只有代表这个Bean类的类型（Type）才可能是ParameterizedType
     * 4，实现泛型参数，可以是给泛型传入了一个真实的类型，或者传入另一个新声明的泛型参数；只声明泛型而不实现，instanceof ParameterizedType为false
     */
    @Test
    public void test1() {
        Grand child1 = new Child();
        Grand child2_1 = new Child2();
        Grand child2_2 = new Child2<String, String>();
        Child2<String, String> child2_3 = new Child2<String, String>();
        Child3<String, String> child3 = new Child3<String, String>();

        // flase
        System.out.println(child1 instanceof ParameterizedType);

        // flase
        System.out.println(child2_1 instanceof ParameterizedType);

        // flase
        System.out.println(child2_2 instanceof ParameterizedType);

        // flase
        System.out.println(child2_3 instanceof ParameterizedType);

        // true
        System.out.println(child1.getClass().getGenericSuperclass() instanceof ParameterizedType);

        // true
        System.out.println(child2_1.getClass().getGenericSuperclass() instanceof ParameterizedType);

        // flase
        System.out.println(child3.getClass().getGenericSuperclass() instanceof ParameterizedType);
    }
}
