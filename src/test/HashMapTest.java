package test;

import org.junit.Test;

import java.util.HashMap;

/**
 * @author dingrui
 * @create 2020-09-02
 * @Description
 */
public class HashMapTest {

    @Test
    public void test1() {
        HashMap<String, String> hashMap = new HashMap<>(17);
        hashMap.put("key-1", "value-1");
        System.out.println();
    }

}
