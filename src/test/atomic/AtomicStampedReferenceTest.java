package test.atomic;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 *@author dingrui
 *@date 2021-02-01
 *@description
 */
public class AtomicStampedReferenceTest {

    @Test
    public void test1() {
        AtomicStampedReference<String> reference = new AtomicStampedReference<String>("ding1", 1);
    }
}
