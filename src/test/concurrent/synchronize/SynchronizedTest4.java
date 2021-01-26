package test.concurrent.synchronize;

/**
 *@author dingrui
 *@date 2021-01-26
 *@description
 */
public class SynchronizedTest4 {

    private Object object = new Object();

    /**
     * @author dingrui
     * @date 2021/1/26
     * @return
     * @description JIT编译器会利锁消除技术进行代码优化 使synchronized不起作用
     */
    public void method1() {
        Object o = new Object();
        synchronized (o) {
            System.out.println("method1");
        }
    }

    /**
     * @author dingrui
     * @date 2021/1/26
     * @return
     * @description JIT编译器会利用锁粗化进行代码优化
     */
    public void method2() {
        synchronized (object) {
            System.out.println("method2-1");
        }

        synchronized (object) {
            System.out.println("method2-2");
        }

        synchronized (object) {
            System.out.println("method2-3");
        }
    }
}
