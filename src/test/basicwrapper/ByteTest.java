package test.basicwrapper;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-12
 *@description
 * 初始化的方式推荐使用Byte的静态方法valueOf 使用缓存的思想 节省性能开销
 */
public class ByteTest {

    @Test
    public void test1() {
        Byte b = new Byte("1");
        System.out.println();
    }

    @Test
    public void test2() {
        Byte b = Byte.valueOf("1");
    }
}
