package test.concurrentskiplistmap;

import org.junit.Test;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 *@author dingrui
 *@date 2021-01-25
 *@description
 */
public class ConcurrentSkipListMapTest {

    /**
     * @author dingrui
     * @date 2021/1/27
     * @return
     * @description 无参构造方法
     */
    @Test
    public void test1() {
        ConcurrentSkipListMap<String, Integer> map = new ConcurrentSkipListMap<>();
    }

    /**
     * @author dingrui
     * @date 2021/1/27
     * @return
     * @description put元素
     * 根据debug的流程 最终形成的数据结构是一个单链表 运气不好 (rnd & 0x80000001)!=0 没有维护索引和层次
     * +-+    +-----+
     * | | -> |ding1|
     * +-+    +-----+
     * 单链表维护了一个头节点 key=null value=object next指向了put进去的节点
     * put进去的节点 key=ding1 value=1 next=null
     */
    @Test
    public void test2() {
        ConcurrentSkipListMap<String, Integer> map = new ConcurrentSkipListMap<>();
        // Integer内部有个cache -128到128直接从缓存里面返回
        map.put("ding1", 1);
    }

    /**
     * @author dingrui
     * @date 2021/1/27
     * @return
     * @description put元素
     * 根据debug的流程 最终形成的是一个跳表 put完第一个元素之后建立了索引层次 put完第二个元素之后没有建立索引和层次
     * +-+      +-+
     * |2|  ->  | |
     * +-+      +-+
     *  |        |
     * +-+      +-+
     * |1| ->   | |
     * +-+      +-+
     *  |        |
     * +-+    +-----+    +-----+
     * | | -> |ding1| -> |ding2|
     * +-+    +-----+    +-----+
     *
     * 根据索引进行查找的逻辑：
     * 1，维护了head属性执行了现在最高层级的HeadIndex 现在这种就是2
     * 2，HeadIndex中有right和down指针 顺着head先向右再想左去找第一个目标节点的key<=需要插入的key
     */
    @Test
    public void test3() {
        ConcurrentSkipListMap<String, Integer> map = new ConcurrentSkipListMap<>();
        map.put("ding1", 1);
        map.put("ding2", 2);
    }
}
