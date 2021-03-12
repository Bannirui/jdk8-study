package test.string;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-11
 *@description
 * 1，基本所有的实现都在基类AbstractStringBuilder中
 * 2，保证线程安全的方式是使用synchronized锁同步
 * 3，构造方法指定字符数组的大小无非就3种情况：长度为16 长度为16+字符串长度 长度为指定的长度
 * 4，维护了一个属性toStringCache 这个缓存是针对toString方法的 缓存的是上一次toString方法的结果 如果sb被修改 缓存被置空
 */
public class StringBufferTest {

    @Test
    public void test1() {
        StringBuffer sb = new StringBuffer();
        System.out.println();
    }
}
