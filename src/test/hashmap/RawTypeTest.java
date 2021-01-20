package test.hashmap;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *@author dingrui
 *@date 2021-01-08
 *@description
 */
public class RawTypeTest {

    interface IG<X,Y>{}
    interface IA<X,Y>{}
    interface IB extends IG{}
    interface IC<X>{}
    interface ID<X>{}
    class Grand<X> implements IA<String,Integer>,IB,IC<X>,ID{}

    @Test
    public void test1() {
        Grand grand = new Grand();
        // IA IB IC ID
        Type[] types = grand.getClass().getGenericInterfaces();
        if (types != null) {
            for (Type type : types) {
                if(type instanceof ParameterizedType){
                    // interface test.hashmap.RawTypeTest$IA
                    // interface test.hashmap.RawTypeTest$IC
                    System.out.println(((ParameterizedType) type).getRawType());
                }
            }
        }

        System.out.println();
    }
}
