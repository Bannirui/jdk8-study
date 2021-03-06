/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A {@linkplain BlockingQueue blocking queue} in which each insert
 * operation must wait for a corresponding remove operation by another
 * thread, and vice versa.  A synchronous queue does not have any
 * internal capacity, not even a capacity of one.  You cannot
 * {@code peek} at a synchronous queue because an element is only
 * present when you try to remove it; you cannot insert an element
 * (using any method) unless another thread is trying to remove it;
 * you cannot iterate as there is nothing to iterate.  The
 * <em>head</em> of the queue is the element that the first queued
 * inserting thread is trying to add to the queue; if there is no such
 * queued thread then no element is available for removal and
 * {@code poll()} will return {@code null}.  For purposes of other
 * {@code Collection} methods (for example {@code contains}), a
 * {@code SynchronousQueue} acts as an empty collection.  This queue
 * does not permit {@code null} elements.
 *
 * <p>Synchronous queues are similar to rendezvous channels used in
 * CSP and Ada. They are well suited for handoff designs, in which an
 * object running in one thread must sync up with an object running
 * in another thread in order to hand it some information, event, or
 * task.
 *
 * <p>This class supports an optional fairness policy for ordering
 * waiting producer and consumer threads.  By default, this ordering
 * is not guaranteed. However, a queue constructed with fairness set
 * to {@code true} grants threads access in FIFO order.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @param <E> the type of elements held in this collection
 */
public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable { // 既然这个同步队列没有存储功能 需要线程之间不停的转移元素 否则就一直阻塞着 什么场景才会有这种奇葩的需求呢 再者 即使真有这种需求 怎么考量生产者-消费者的处理速度呢 会导致整个模型阻塞
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * This class implements extensions of the dual stack and dual
     * queue algorithms described in "Nonblocking Concurrent Objects
     * with Condition Synchronization", by W. N. Scherer III and
     * M. L. Scott.  18th Annual Conf. on Distributed Computing,
     * Oct. 2004 (see also
     * http://www.cs.rochester.edu/u/scott/synchronization/pseudocode/duals.html).
     * The (Lifo) stack is used for non-fair mode, and the (Fifo)
     * queue for fair mode. The performance of the two is generally
     * similar. Fifo usually supports higher throughput under
     * contention but Lifo maintains higher thread locality in common
     * applications.
     *
     * A dual queue (and similarly stack) is one that at any given
     * time either holds "data" -- items provided by put operations,
     * or "requests" -- slots representing take operations, or is
     * empty. A call to "fulfill" (i.e., a call requesting an item
     * from a queue holding data or vice versa) dequeues a
     * complementary node.  The most interesting feature of these
     * queues is that any operation can figure out which mode the
     * queue is in, and act accordingly without needing locks.
     *
     * Both the queue and stack extend abstract class Transferer
     * defining the single method transfer that does a put or a
     * take. These are unified into a single method because in dual
     * data structures, the put and take operations are symmetrical,
     * so nearly all code can be combined. The resulting transfer
     * methods are on the long side, but are easier to follow than
     * they would be if broken up into nearly-duplicated parts.
     *
     * The queue and stack data structures share many conceptual
     * similarities but very few concrete details. For simplicity,
     * they are kept distinct so that they can later evolve
     * separately.
     *
     * The algorithms here differ from the versions in the above paper
     * in extending them for use in synchronous queues, as well as
     * dealing with cancellation. The main differences include:
     *
     *  1. The original algorithms used bit-marked pointers, but
     *     the ones here use mode bits in nodes, leading to a number
     *     of further adaptations.
     *  2. SynchronousQueues must block threads waiting to become
     *     fulfilled.
     *  3. Support for cancellation via timeout and interrupts,
     *     including cleaning out cancelled nodes/threads
     *     from lists to avoid garbage retention and memory depletion.
     *
     * Blocking is mainly accomplished using LockSupport park/unpark,
     * except that nodes that appear to be the next ones to become
     * fulfilled first spin a bit (on multiprocessors only). On very
     * busy synchronous queues, spinning can dramatically improve
     * throughput. And on less busy ones, the amount of spinning is
     * small enough not to be noticeable.
     *
     * Cleaning is done in different ways in queues vs stacks.  For
     * queues, we can almost always remove a node immediately in O(1)
     * time (modulo retries for consistency checks) when it is
     * cancelled. But if it may be pinned as the current tail, it must
     * wait until some subsequent cancellation. For stacks, we need a
     * potentially O(n) traversal to be sure that we can remove the
     * node, but this can run concurrently with other threads
     * accessing the stack.
     *
     * While garbage collection takes care of most node reclamation
     * issues that otherwise complicate nonblocking algorithms, care
     * is taken to "forget" references to data, other nodes, and
     * threads that might be held on to long-term by blocked
     * threads. In cases where setting to null would otherwise
     * conflict with main algorithms, this is done by changing a
     * node's link to now point to the node itself. This doesn't arise
     * much for Stack nodes (because blocked threads do not hang on to
     * old head pointers), but references in Queue nodes must be
     * aggressively forgotten to avoid reachability of everything any
     * node has ever referred to since arrival.
     */

    /**
     * Shared internal API for dual stacks and queues.
     */
    abstract static class Transferer<E> { // 抽象类 定义了一个transfer方法传输元素
        /**
         * Performs a put or take.
         *
         * @param e if non-null, the item to be handed to a consumer;
         *          if null, requests that transfer return an item
         *          offered by producer.
         * @param timed if this operation should timeout
         * @param nanos the timeout, in nanoseconds
         * @return if non-null, the item provided or received; if null,
         *         the operation failed due to timeout or interrupt --
         *         the caller can distinguish which of these occurred
         *         by checking Thread.interrupted.
         */
        abstract E transfer(E e, boolean timed, long nanos);
    }

    /** The number of CPUs, for spin control */
    static final int NCPUS = Runtime.getRuntime().availableProcessors(); // CPU数量

    /**
     * The number of times to spin before blocking in timed waits.
     * The value is empirically derived -- it works well across a
     * variety of processors and OSes. Empirically, the best value
     * seems not to vary with number of CPUs (beyond 2) so is just
     * a constant.
     */
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32; // 有超时的时候自旋多少次 32次 当cpu数量小于2的时候不自旋 0或者32

    /**
     * The number of times to spin before blocking in untimed waits.
     * This is greater than timed value because untimed waits spin
     * faster since they don't need to check times on each spin.
     */
    static final int maxUntimedSpins = maxTimedSpins * 16; // 没有超时的时候自旋多少次 0或者512

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices.
     */
    static final long spinForTimeoutThreshold = 1000L; // 针对有超时的情况 自旋多少次后 如果剩余时间大于1000纳秒就使用带时间的

    /** Dual stack */
    static final class TransferStack<E> extends Transferer<E> { // 以栈的方式实现的Transfer
        /*
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */

        /* Modes for SNodes, ORed together in node fields */
        /** Node represents an unfulfilled consumer */
        static final int REQUEST    = 0; // 栈中节点类型 消费者 请求数据 代表执行的是take方法
        /** Node represents an unfulfilled producer */
        static final int DATA       = 1; // 栈中节点类型 生产者 提供数据 代表执行的是put方法
        /** Node is fulfilling another unfulfilled DATA or REQUEST */
        static final int FULFILLING = 2; // 栈中节点类型 二者正在匹配中 代表栈的头节点正在阻塞等待其他线程进行put或者take操作

        /** Returns true if m has fulfilling bit set. */ // FULFILLING是2 m表示的是SNode的mode属性值 只有3个值：0(REQUEST)、1(DATA)、2(FULFILLING) 只有当m是2的时候才会返回true 也就是说判断当前是否是FULFILLING
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; } // 判断栈是否处于fulfilling状态

        /** Node class for TransferStacks. */
        static final class SNode { // 栈中的节点
            volatile SNode next;        // next node in stack // 下一个节点 栈的下一个 就是被当前栈压在下面的元素
            volatile SNode match;       // the node matched to this // 匹配者 用来判断阻塞栈元素能被唤醒的时机 假设先执行take 此时队列中没有数据 take被阻塞 当有put操作时会把阻塞的栈元素的match属性赋值 当阻塞的栈元素发现match属性有值时就会停止阻塞
            volatile Thread waiter;     // to control park/unpark // 等待这的线程 被阻塞的线程 栈中的元素是无法实现阻塞的 是通过线程阻塞来实现的 waiter为阻塞的线程
            Object item;                // data; or null for REQUESTs // 元素 来投递的消息或者未消费的消息
            int mode; // 模式 也就是节点的类型：消费者、生产者、匹配中 操作的模式
            // Note: item and mode fields don't need to be volatile
            // since they are always written before, and read after,
            // other volatile/atomic operations.

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) { // cas方法设置next
                return cmp == next &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val); // 当前节点的next域由cmp换成val
            }

            /**
             * Tries to match node s to this node, if so, waking up thread.
             * Fulfillers call tryMatch to identify their waiters.
             * Waiters block until they have been matched.
             *
             * @param s the node to match
             * @return true if successfully matched to s
             */
            boolean tryMatch(SNode s) { // SNode里面的方向 调用者m是s的下一个节点 这时候m节点的线程应该是阻塞状态 阻塞等待match非null cas设置match为s节点
                if (match == null &&
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) { // 如果m还没有匹配者 就把s作为它的匹配者
                    Thread w = waiter; // wait属性awaitFullfill方法中被赋值 在需要阻塞节点的情况下被赋值
                    if (w != null) {    // waiters need at most one unpark
                        waiter = null;
                        LockSupport.unpark(w); // 唤醒m中的线程 两者匹配完毕
                    }
                    return true; // 匹配到了返回true
                }
                return match == s; // 可能其他线程先一步匹配了m 返回其是否是s 在match非null的情况下直接返回match与s的比较结果
            }

            /**
             * Tries to cancel a wait by matching node to itself.
             */
            void tryCancel() { // cas设置match为当前节点
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            boolean isCancelled() {
                return match == this; // 判断match是否被取消
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset; // 属性match在SNode对象中内存地址偏移量
            private static final long nextOffset; // 属性next在SNode对象中内存地址偏移量

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = SNode.class;
                    matchOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("match"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** The head (top) of the stack */
        volatile SNode head; // 栈中的头节点

        boolean casHead(SNode h, SNode nh) { // 把当前节点的头节点h换成nh put场景：h=null nh=SNode(item=元素, next=null, match=null, waiter=null, mode=REQUEST)
            return h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh); // cas设置头节点
        }

        /**
         * Creates or resets fields of a node. Called only from transfer
         * where the node to push on stack is lazily created and
         * reused when possible to help reduce intervals between reads
         * and CASes of head and to avoid surges of garbage when CASes
         * to push nodes fail due to contention.
         */
        static SNode snode(SNode s, Object e, SNode next, int mode) { // put场景：s=null e=元素 next=null mode=REQUEST cas设置头节点 s为当前新建的节点 next为当前节点的下一个节点 mode为当前节点的模式 e为保存于节点中的元素
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }

        /**
         * Puts or takes an item.
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) { // 从put方法进来的时候e是要生产者表示要放的元素 从take方法进来的时候e是null表示要取元素 put场景：put方法进来 take场景：take方法进来
            /*
             * Basic algorithm is to loop trying one of three actions:
             *
             * 1. If apparently empty or already containing nodes of same
             *    mode, try to push node on stack and wait for a match,
             *    returning it, or null if cancelled.
             *
             * 2. If apparently containing node of complementary mode,
             *    try to push a fulfilling node on to stack, match
             *    with corresponding waiting node, pop both from
             *    stack, and return matched item. The matching or
             *    unlinking might not actually be necessary because of
             *    other threads performing action 3:
             *
             * 3. If top of stack already holds another fulfilling node,
             *    help it out by doing its match and/or pop
             *    operations, and then continue. The code for helping
             *    is essentially the same as for fulfilling, except
             *    that it doesn't return the item.
             */

            SNode s = null; // constructed/reused as needed
            int mode = (e == null) ? REQUEST : DATA; // 根据e是否为null决定是生产者还是消费者 put的时候e是有值的 mode=DATA take的时候e是null mode=REQUEST

            for (;;) { // 自旋+cas
                SNode h = head; // 栈顶元素 栈里面最多只有一个元素 如果此前没有put进来过元素就是null 如果put过还没take走 head就是那个元素 获取头节点有以下3种情况：1，头节点为空说明队列中还没有数据 2，头节点不为空并且是take类型的说明有线程正等着拿数据 3，头节点不为空并且是put类型说明有线程正等着放数据
                if (h == null || h.mode == mode) {  // empty or same-mode // 栈顶没有元素 或者栈顶元素模式跟当前元素模式是一样的(也就是都是生产者节点或者都是消费者节点 上一次是put这次也是put 上次是take这次也是take)
                    if (timed && nanos <= 0) {      // can't wait // 如果有超时而且已经到期了 设置了超时时间并且e元素进栈或者出栈超时则丢弃本次操作直接返回null值 如果栈头此时被取消了 丢弃栈头 取下一个节点继续消费
                        if (h != null && h.isCancelled()) // 如果头节点不为空而且是取消状态
                            casHead(h, h.next);     // pop cancelled node // 就把头节点弹出 并且进入下一次循环
                        else // 栈头是空的 直接返回null
                            return null; // 否则直接返回null(超时返回null)
                    } else if (casHead(h, s = snode(s, e, h, mode))) { // 因为模式相同 只能入栈 如果入栈成功 s=SNode(next=h,match=null,waiter=null,item=e,mode=mode) 如果头节点h为空就把头节点换成s 没有超时直接将e元素作为新的栈头
                        SNode m = awaitFulfill(s, timed, nanos); // 调用awaitFulfill()方法自旋+阻塞当前入栈的线程并等待被匹配到 e等待出栈 一种是空队列take 一种是put 返回s表示空队列一直没有数据或者put数据一直没有人收
                        if (m == s) {               // wait was cancelled // 如果m等于s 说明取消了 那么就把它清除 并返回null
                            clean(s); // 做一次清理
                            return null; // 被取消了返回null
                        } // 代码走到这里 说明匹配到元素了 因为从awaitFulfill()方法里面出来要么被取消了 要么就匹配到了 表名来了一次take/put请求并且take与put匹配成功 此时需要弹出两个节点
                        if ((h = head) != null && h.next == s) // 如果头节点不为空 并且头节点的下一个节点是s 就把头节点换成s的下一个节点 也就是把h和s都弹出 也就是把栈顶两个元素都弹出
                            casHead(h, s.next);     // help s's fulfiller
                        return (E) ((mode == REQUEST) ? m.item : s.item); // 根据当前节点的模式判断返回m还是s中的值 对于take方法返回m的数据 对于put方法返回s的数据
                    }
                } else if (!isFulfilling(h.mode)) { // try to fulfill // 代码到这说明头节点和当前节点的模式不一样(头节点是put当前是take) 如果头节点不是正在匹配中 栈头正在等待其他线程put或者take 执行到这说明头节点不为null并且两次请求的模式不同
                    if (h.isCancelled())            // already cancelled // 如果头节点已经取消了 就把它弹出栈
                        casHead(h, h.next);         // pop and retry
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) { // 头节点没有在匹配中 就让当前节点入队 再让它们尝试匹配 并且s成为了新的头节点 它的状态是正在匹配中
                        for (;;) { // loop until matched or waiters disappear
                            SNode m = s.next;       // m is s's match m是插入s之前的栈头节点
                            if (m == null) {        // all waiters are gone // 如果m是null 说明除了s节点外的节点都被其他线程抢先一步匹配掉了 就清空栈并跳出内部循环 到外部循环再重新入栈判断
                                casHead(s, null);   // pop fulfill node
                                s = null;           // use new node next time
                                break;              // restart main loop
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) { // 如果m和s尝试匹配成功 就弹出栈顶的两个元素m和s tryMatch这个方法有2个作用：1，唤醒被阻塞的栈头m 2，把当前节点s赋值给m的match属性 这样m被唤醒时就能从match中得到本次操作s 其中s.item可以记录着本次的操作节点
                                casHead(s, mn);     // pop both s and m
                                return (E) ((mode == REQUEST) ? m.item : s.item); // 返回匹配的结果 对于take方法返回m的数据 对于put方法返回s的数据
                            } else                  // lost match
                                s.casNext(m, mn);   // help unlink // 尝试匹配失败 说明m已经先一步被其他线程匹配了 就协助清除它 将m出栈继续下一轮循环
                        }
                    }
                } else {                            // help a fulfiller // 到这说明当前节点和头节点模式不一样 且头节点是正在匹配中
                    SNode m = h.next;               // m is h's match
                    if (m == null)                  // waiter is gone // 如果m为null 说明m已经被其他线程先一步匹配了
                        casHead(h, null);           // pop fulfilling node
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          // help match // 协助匹配 如果m和s尝试匹配成功 就弹出栈顶的两个元素m和s
                            casHead(h, mn);         // pop both h and m // 将栈顶的两个元素弹出后 再让s重新入栈 此时m的match属性为h则h、m出栈
                        else                        // lost match // 尝试匹配失败 说明m已经先一步被其他线程匹配了 就协助清除它
                            h.casNext(m, mn);       // help unlink 此时m的match属性不为h则m出栈
                    }
                }
            }
        }

        /**
         * Spins/blocks until node s is matched by a fulfill operation.
         *
         * @param s the waiting node 需要等待的节点
         * @param timed true if timed wait 是否需要超时
         * @param nanos timeout value 超时的时间
         * @return matched node, or s if cancelled
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            /*
             * When a node/thread is about to block, it sets its waiter
             * field and then rechecks state at least one more time
             * before actually parking, thus covering race vs
             * fulfiller noticing that waiter is non-null so should be
             * woken.
             *
             * When invoked by nodes that appear at the point of call
             * to be at the head of the stack, calls to park are
             * preceded by spins to avoid blocking when producers and
             * consumers are arriving very close in time.  This can
             * happen enough to bother only on multiprocessors.
             *
             * The order of checks for returning out of main loop
             * reflects fact that interrupts have precedence over
             * normal returns, which have precedence over
             * timeouts. (So, on timeout, one last check for match is
             * done before giving up.) Except that calls from untimed
             * SynchronousQueue.{poll/offer} don't check interrupts
             * and don't wait at all, so are trapped in transfer
             * method rather than calling awaitFulfill.
             */
            final long deadline = timed ? System.nanoTime() + nanos : 0L; // 到期时间deadline
            Thread w = Thread.currentThread(); // 当前线程w 获取当前线程
            int spins = (shouldSpin(s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0); // 自旋次数 0或者32或者512 如果设置了超时时间自旋32次 否则自旋512次 比如take操作自旋结束后还没有其他线程的put进来就会阻塞等待 有超时时间的阻塞固定时间 否则一直阻塞下去
            for (;;) { // 自旋
                if (w.isInterrupted()) // 当前线程中断了 尝试清除s
                    s.tryCancel(); // 尝试取消match 将s节点的match属性设置为s节点本身 那么下一轮循环可以获取match并退出
                SNode m = s.match; // 检查s是否匹配到了元素m 有可能是其他线程的m匹配到了当前线程的s
                if (m != null) // 如果匹配到了 就直接返回m
                    return m;
                if (timed) { // 如果需要超时
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) { // 检查超时时间<=0了 就尝试清除s
                        s.tryCancel(); // 超时则尝试取消match 将s节点的match属性设置为s节点本身 那么下一轮循环可以获取match并退出
                        continue;
                    }
                } // 下面的if...else if...else if...else if在自旋的一次循环之中只会进入一个分支 如果代码走到这 优先自旋spins次数
                if (spins > 0) // 如果还有自旋次数 自旋次数-1 并进入下一次的自旋 优先自旋spins次数
                    spins = shouldSpin(s) ? (spins-1) : 0;
                else if (s.waiter == null) // 自旋次数spins没有了的情况下
                    s.waiter = w; // establish waiter so can park next iter // 如果s的waiter是null 把当前线程注入进去 并进入下一次自旋
                else if (!timed) // 如果不允许超时 直接阻塞 并等待被其他线程唤醒 唤醒后继续自旋并查看是否匹配到了元素
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold) // 如果允许超时并且还有剩余时间 就阻塞相应的时间
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Returns true if node s is at head or there is an active
         * fulfiller.
         */
        boolean shouldSpin(SNode s) { // 判断是否可以自旋
            SNode h = head; // 获取头节点
            return (h == s || h == null || isFulfilling(h.mode)); // 需要自旋的场景：传进来的是头节点、头节点为空、头节点的mode是put DATA
        }

        /**
         * Unlinks s from the stack.
         */
        void clean(SNode s) {
            s.item = null;   // forget item item表示节点中待传递的消息
            s.waiter = null; // forget thread waiter表示节点中的线程 用于阻塞节点

            /*
             * At worst we may need to traverse entire stack to unlink
             * s. If there are multiple concurrent calls to clean, we
             * might not see s if another thread has already removed
             * it. But we can stop when we see any node known to
             * follow s. We use s.next unless it too is cancelled, in
             * which case we try the node one past. We don't check any
             * further because we don't want to doubly traverse just to
             * find sentinel.
             */

            SNode past = s.next; // 指针指向s节点的下一节点
            if (past != null && past.isCancelled()) // 确定s之后连续的被取消的节点
                past = past.next;

            // Absorb cancelled nodes at head
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled()) // 以头节点为起点 past为终点 删除这个区间内从头节点开始连续被取消的节点
                casHead(p, p.next);

            // Unsplice embedded nodes
            while (p != null && p != past) { // 删除连续已经取消的节点
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset; // TransferStack中属性head头节点在TransferStack对象中内存地址偏移量
        static { // 静态代码块中初始化UNSAFE和headOffset
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** Dual Queue */
    static final class TransferQueue<E> extends Transferer<E> { // 以队列方式实现的Transfer 双向队列 FIFO 公平
        /*
         * This extends Scherer-Scott dual queue algorithm, differing,
         * among other ways, by using modes within nodes rather than
         * marked pointers. The algorithm is a little simpler than
         * that for stacks because fulfillers do not need explicit
         * nodes, and matching is done by CAS'ing QNode.item field
         * from non-null to null (for put) or vice versa (for take).
         */

        /** Node class for TransferQueue. */
        static final class QNode { // 队列中的节点
            volatile QNode next;          // next node in queue // 下一个节点 当前元素的下一个元素
            volatile Object item;         // CAS'ed to or from null // 存储的元素 当前元素的值
            volatile Thread waiter;       // to control park/unpark // 等待着的线程 可以阻塞住当前线程 队列中元素是通过线程进行阻塞的
            final boolean isData; // 是否是数据节点

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) { // cas设置next
                return next == cmp &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) { // cas设置item
                return item == cmp &&
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            /**
             * Tries to cancel by CAS'ing ref to this as item.
             */
            void tryCancel(Object cmp) { // cas设置item为当前节点
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() { // 判断是否已经被取消
                return item == this;
            }

            /**
             * Returns true if this node is known to be off the queue
             * because its next pointer has been forgotten due to
             * an advanceHead operation.
             */
            boolean isOffList() { // 判断该节点是否离队
                return next == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset; // QNode类中属性item在QNode对象中的内存地址偏移量
            private static final long nextOffset; // QNode类中属性next在QNode对象中的内存地址偏移量

            static { // 静态代码块初始化属性
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** Head of queue */
        transient volatile QNode head; // 队列的头节点
        /** Tail of queue */
        transient volatile QNode tail; // 队列的尾节点
        /**
         * Reference to a cancelled node that might not yet have been
         * unlinked from queue because it was the last inserted node
         * when it was cancelled.
         */
        transient volatile QNode cleanMe; // 表示队列中待清除的节点

        TransferQueue() {
            QNode h = new QNode(null, false); // initialize to dummy node.
            head = h;
            tail = h;
        }

        /**
         * Tries to cas nh as new head; if successful, unlink
         * old head's next node to avoid garbage retention.
         */
        void advanceHead(QNode h, QNode nh) { // 设置新的头节点
            if (h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next
        }

        /**
         * Tries to cas nt as new tail.
         */
        void advanceTail(QNode t, QNode nt) { // 设置新的尾节点
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        /**
         * Tries to CAS cleanMe slot.
         */
        boolean casCleanMe(QNode cmp, QNode val) { // cas设置cleanMe
            return cleanMe == cmp &&
                UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * Puts or takes an item.
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /* Basic algorithm is to loop trying to take either of
             * two actions:
             *
             * 1. If queue apparently empty or holding same-mode nodes,
             *    try to add node to queue of waiters, wait to be
             *    fulfilled (or cancelled) and return matching item.
             *
             * 2. If queue apparently contains waiting items, and this
             *    call is of complementary mode, try to fulfill by CAS'ing
             *    item field of waiting node and dequeuing it, and then
             *    returning matching item.
             *
             * In each case, along the way, check for and try to help
             * advance head and tail on behalf of other stalled/slow
             * threads.
             *
             * The loop starts off with a null check guarding against
             * seeing uninitialized head or tail values. This never
             * happens in current SynchronousQueue, but could if
             * callers held non-volatile/final ref to the
             * transferer. The check is here anyway because it places
             * null checks at top of loop, which is usually faster
             * than having them implicitly interspersed.
             */

            QNode s = null; // constructed/reused as needed
            boolean isData = (e != null); // true是put false是take

            for (;;) {
                QNode t = tail; // 队列头和尾的临时变量 队列是空的时候t==h
                QNode h = head;
                if (t == null || h == null)         // saw uninitialized value
                    continue;                       // spin

                if (h == t || t.isData == isData) { // empty or same-mode h==t收尾节点相同说明队列是空的 t.isData==isData尾节点的操作和当前节点的操作一致
                    QNode tn = t.next;
                    if (t != tail)                  // inconsistent read t不等于tail说明tail被修改过了
                        continue;
                    if (tn != null) {               // lagging tail 队尾后面的元素不是空的 说明t还不是队尾 直接把tn赋值给t 保证t是队尾元素 进一步加强校验
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // can't wait 如果超时了就直接返回null
                        return null;
                    if (s == null) // 构造Node节点
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        // failed to link in 如果把e放到队尾失败就继续递归放置
                        continue;

                    advanceTail(t, s);              // swing tail and wait 将s节点入队
                    Object x = awaitFulfill(s, e, timed, nanos); // 阻塞住自己
                    if (x == s) {                   // wait was cancelled x==s表明s节点被取消了
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // not already unlinked s仍在队列中就出队s
                        advanceHead(t, s);          // unlink if head
                        if (x != null)              // and forget fields
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null) ? (E)x : e;

                } else {                            // complementary-mode 队列不为空并且队尾节点操作和当前节点操作不一样 也就是说当前节点和队尾节点是一对可以匹配的操作
                    QNode m = h.next;               // node to fulfill 如果是第一次执行 这里的m就是tail 这行代码体现了队列的公平性 每次操作时都是从头开始按照顺序进行操作
                    if (t != tail || m == null || h != head)
                        continue;                   // inconsistent read

                    Object x = m.item;
                    if (isData == (x != null) ||    // m already fulfilled 判断操作类型
                        x == m ||                   // m cancelled 判断是否取消
                        !m.casItem(x, e)) {         // lost CAS 这里把当前的操作值赋值给已经阻塞住的m的match属性 这样m被释放时就可以得到此次操作的值
                        advanceHead(h, m);          // dequeue and retry
                        continue;
                    }

                    advanceHead(h, m);              // successfully fulfilled 当前操作被放到队头
                    LockSupport.unpark(m.waiter); // 唤醒线程 释放队头阻塞节点
                    return (x != null) ? (E)x : e;
                }
            }
        }

        /**
         * Spins/blocks until node s is fulfilled.
         *
         * @param s the waiting node
         * @param e the comparison value for checking match
         * @param timed true if timed wait
         * @param nanos timeout value
         * @return matched item, or s if cancelled
         */
        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            /* Same idea as TransferStack.awaitFulfill */
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread(); // 获取当前线程
            int spins = ((head.next == s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0); // 如果设置了超时时间 自旋32次否则自旋512次 比如本次操作是take 自旋次数之后 仍然没有其他线程put操作进来 就会阻塞 有超时时间的 阻塞固定时间之后就会一直阻塞下去
            for (;;) {
                if (w.isInterrupted()) // 线程有没有被打断 如果过了超时时间 当前线程就会被中断
                    s.tryCancel(e); // 尝试取消 设置item为当前节点 那么下一轮便可以获取item并退出
                Object x = s.item;
                if (x != e)
                    return x;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        s.tryCancel(e); // 超时尝试取消 设置item为当前节点则下一轮便可以获取item并退出
                        continue;
                    }
                }
                if (spins > 0)
                    --spins; // 自旋次数减1 在需要自旋的情况下不会阻塞当前线程
                else if (s.waiter == null) // 自旋结束之后还没有得到匹配的线程
                    s.waiter = w; // 设置阻塞的线程
                else if (!timed)
                    LockSupport.park(this); // 通过park进行线程阻塞
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos); // 进行线程的定时阻塞
            }
        }

        /**
         * Gets rid of cancelled node s with original predecessor pred.
         */
        void clean(QNode pred, QNode s) {
            s.waiter = null; // forget thread 清除节点对应的线程 waiter为节点中封装的线程 用于阻塞节点
            /*
             * At any given time, exactly one node on list cannot be
             * deleted -- the last inserted node. To accommodate this,
             * if we cannot delete s, we save its predecessor as
             * "cleanMe", deleting the previously saved version
             * first. At least one of node s or the node previously
             * saved can always be deleted, so this always terminates.
             */
            while (pred.next == s) { // Return early if already unlinked
                QNode h = head;
                QNode hn = h.next;   // Absorb cancelled first node as head
                if (hn != null && hn.isCancelled()) { // 从头节点开始 取消的节点前移
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // Ensure consistent read for tail
                if (t == h) // 队列为空
                    return;
                QNode tn = t.next;
                if (t != tail) // 防止清理的过程中有数据插入
                    continue;
                if (tn != null) {
                    advanceTail(t, tn); // 防止清理的过程中有数据插入
                    continue;
                }
                if (s != t) {        // If not tail, try to unsplice s不是尾节点
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn)) // sn==s表明s已经出队 pred.casNext(s, sn)表明s为pred的下一个节点并且出队成功
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // Try unlinking previous cancelled node
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d is gone or
                        d == dp ||                 // d is off list or d离队
                        !d.isCancelled() ||        // d not cancelled or d没有取消
                        (d != t &&                 // d not tail and d不是尾节点
                         (dn = d.next) != null &&  //   has successor d有后继节点
                         dn != d &&                //   that is on list d的后继节点在队列中
                         dp.casNext(d, dn)))       // d unspliced d出队
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      // s is already saved node
                } else if (casCleanMe(null, pred))
                    return;          // Postpone cleaning s
            }
        }

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset; // TransferQueue类中属性head在TransferQueue对象中内存地址偏移量
        private static final long tailOffset; // TransferQueue类中属性tail在TransferQueue对象中内存地址偏移量
        private static final long cleanMeOffset; // TransferQueue类中属性cleanMe在TransferQueue对象中内存地址偏移量
        static { // 静态代码块中初始化UNSAFE、head偏移量、tail偏移量、cleanMe偏移量
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * The transferer. Set only in constructor, but cannot be declared
     * as final without further complicating serialization.  Since
     * this is accessed only at most once per public method, there
     * isn't a noticeable performance penalty for using volatile
     * instead of final here.
     */
    private transient volatile Transferer<E> transferer; // 传输器 两个线程之间交换元素使用 内部实现了2种数据结构 公平方式：队列TransferQueue 非公平方式：堆栈TransferStack

    /**
     * Creates a {@code SynchronousQueue} with nonfair access policy.
     */
    public SynchronousQueue() { // 队列不存储数据 没有大小 没发迭代 插入操作的返回必须等待另一个线程的删除操作 反之亦然 队列有两种数据结构 分别是FIFO的队列和FILO的堆栈 队列是公平的 堆栈是非公平的
        this(false); // 默认非公平模式 也就是使用的栈
    }

    /**
     * Creates a {@code SynchronousQueue} with the specified fairness policy.
     *
     * @param fair if true, waiting threads contend in FIFO order for
     *        access; otherwise the order is unspecified.
     */
    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>(); // 如果是公平模式就是队列 如果是非公平模式就是栈
    }

    /**
     * Adds the specified element to this queue, waiting if necessary for
     * another thread to receive it.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException { // 将元素放入队列 直到有另外一个线程从这个队列种取走元素 成功则结束 失败则中断线程
        if (e == null) throw new NullPointerException(); // 元素不能为空
        if (transferer.transfer(e, false, 0) == null) { // 调用传输器具体实现的transfer()方法 三个参数分别是：传输的元素、是否需要超时、超时的时间 传入元素e 说明是生产者
            Thread.interrupted(); // 如果传输失败 直接让线程中断并抛出中断异常
            throw new InterruptedException();
        }
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * up to the specified wait time for another thread to receive it.
     *
     * @return {@code true} if successful, or {@code false} if the
     *         specified waiting time elapses before a consumer appears
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    /**
     * Inserts the specified element into this queue, if another thread is
     * waiting to receive it.
     *
     * @param e the element to add
     * @return {@code true} if the element was added to this queue, else
     *         {@code false}
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * for another thread to insert it.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException { // 从队列头拿数据并将队列种数据删除 成功则返回 失败则中断线程并抛出异常
        E e = transferer.transfer(null, false, 0); // 三个参数：null、是否需要超时、超时时间 第一个参数null表示消费者 要取数据
        if (e != null) // 取到了数据
            return e;
        Thread.interrupted(); // 没取到数据 让线程中断抛出异常
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, waiting
     * if necessary up to the specified wait time, for another thread
     * to insert it.
     *
     * @return the head of this queue, or {@code null} if the
     *         specified waiting time elapses before an element is present
     * @throws InterruptedException {@inheritDoc}
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, if another thread
     * is currently making an element available.
     *
     * @return the head of this queue, or {@code null} if no
     *         element is available
     */
    public E poll() {
        return transferer.transfer(null, true, 0);
    }

    /**
     * Always returns {@code true}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return {@code true}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int size() {
        return 0;
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int remainingCapacity() {
        return 0;
    }

    /**
     * Does nothing.
     * A {@code SynchronousQueue} has no internal capacity.
     */
    public void clear() {
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element
     * @return {@code false}
     */
    public boolean contains(Object o) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element to remove
     * @return {@code false}
     */
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Returns {@code false} unless the given collection is empty.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false} unless given collection is empty
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code null}.
     * A {@code SynchronousQueue} does not return elements
     * unless actively waited on.
     *
     * @return {@code null}
     */
    public E peek() {
        return null;
    }

    /**
     * Returns an empty iterator in which {@code hasNext} always returns
     * {@code false}.
     *
     * @return an empty iterator
     */
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Returns an empty spliterator in which calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return an empty spliterator
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    /**
     * Returns a zero-length array.
     * @return a zero-length array
     */
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Sets the zeroeth element of the specified array to {@code null}
     * (if the array has non-zero length) and returns it.
     *
     * @param a the array
     * @return the specified array
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /*
     * To cope with serialization strategy in the 1.5 version of
     * SynchronousQueue, we declare some unused classes and fields
     * that exist solely to enable serializability across versions.
     * These fields are never used, so are initialized only if this
     * object is ever serialized or deserialized.
     */

    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    /**
     * Saves this queue to a stream (that is, serializes it).
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue<E>();
        else
            transferer = new TransferStack<E>();
    }

    // Unsafe mechanics
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

}
