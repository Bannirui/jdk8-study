package test.basicwrapper;

import org.junit.Test;

/**
 *@author dingrui
 *@date 2021-03-12
 *@description
 */
public class BooleanTest {

    @Test
    public void test1() {
        Boolean b = new Boolean("true");
        System.out.println();
    }

    @Test
    public void test2() {
        boolean a = true;
        boolean b = false;
        boolean c = Boolean.logicalXor(a, b);
        System.out.println();
    }
}
