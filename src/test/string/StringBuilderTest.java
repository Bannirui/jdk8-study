package test.string;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-12
 *@description
 * 1，基本所有的实现都是在基类AbstractStringBuilder中
 * 2，对比StringBuffer
 *     2.1，方法没有synchronized修饰 不是线程安全的
 *     2.2，没有维护缓存属性toStringCache
 */
public class StringBuilderTest {

    @Test
    public void test1() {
        StringBuilder sb = new StringBuilder();
        System.out.println();
    }
}
