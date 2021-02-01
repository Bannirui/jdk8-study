package test.unsafe;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 *@author dingrui
 *@date 2021-02-01
 *@description
 */
public class UnsafeTest {

    private static final String field = "theUnsafe";

    @Test
    public void test1() throws NoSuchFieldException, IllegalAccessException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description new对象实例
     * 通过构造方法实例化这个类，age属性将会返回10
     */
    @Test
    public void test2() {
        User user = new User();
        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description 通过Unsafe进行实例化
     * unsafe.allocateInstance()只会给对象分配内存，并不会调用构造方法，所以这里只会返回int类型的默认值0
     */
    @Test
    public void test3() throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
        User user = (User) unsafe.allocateInstance(User.class);

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021/2/1
     * @return
     * @description 使用Unsafe的putXXX()方法，我们可以修改任意私有字段的值
     * 一旦我们通过反射调用得到字段age，我们就可以使用Unsafe将其值更改为任何其他int值
     */
    @Test
    public void test4() throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field f = Unsafe.class.getDeclaredField(field);
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        User user = new User();
        Field age = user.getClass().getDeclaredField("age");
        unsafe.putInt(user, unsafe.objectFieldOffset(age), 20);

        System.out.println();
    }

    /**
     * @author dingrui
     * @date 2021-02-01
     * @description 测试类User
     */
    class User {
        int age;

        public User() {
            this.age = 10;
        }
    }
}
