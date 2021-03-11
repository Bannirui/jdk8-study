package test.string;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-11
 *@description
 */
public class StringTest {

    @Test
    public void test1() {
        String s = new String();
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/3/11
     * @return
     * @description 字符串截取 闭区间 [index,最后]
     */
    @Test
    public void test2() {
        String s = new String("dingrui");
        String substring = s.substring(1);
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/3/11
     * @return
     * @description 字符串拼接
     */
    @Test
    public void test3() {
        String oldStr = new String("dingrui");
        String newStr = new String("lixiao");
        String concat = oldStr.concat(newStr);
        System.out.println();
    }

    @Test
    public void test4() {
        String oldStr = new String("dingrui");
        String newStr = new String("");
        String concat = oldStr.concat(newStr);
        System.out.println();
    }

    @Test
    public void test5() {
        String s = new String("dingrui");
        String replace = s.replace("i", "_");
        System.out.println();
    }

    @Test
    public void test6() {
        String s = new String("dingrui");
        String s1 = String.valueOf(s);
        System.out.println();
    }
}
