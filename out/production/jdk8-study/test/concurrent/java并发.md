# java并发

> wait、notify

* wait
    * 当调用对象的wait方法时，必须确保调用wait方法的这个线程已经持有了这个对象的monitor监视器锁
    * 调用对象的wait方法后，该线程就会释放掉该对象的监视器锁，然后进入等待状态，进入了monitor的wait set
    * 当线程调用wait后进入等待状态时，它就可以等待其他线程调用相同对象的notify或者notifyAll方法使自己被唤醒
    * 一旦这个线程被其他线程唤醒后，该线程就会与其他线程一起开始竞争这个对象的锁，公平竞争，只有当该线程获取到这个对象的锁之后，线程才会继续往下执行
    * 调用Thread的sleep方法时，线程并不会释放掉对象的锁
    * 调用wait方法的代码片段需要放在一个synchronized块或者synchronized方法中，这样才可以保证线程在调用wait方法之前已经获取到了对象的锁
*  notify
    * 当调用对象的notify方法使，它会随机唤醒这个对象等待集合wait set中的任意一个线程，当某个线程被唤醒之后，它就会与其他线程公平竞争对象的锁
    * 当调用对象的notifyAll方法时，它会唤醒该对象等待集合wait set中的所有线程，这些线程被唤醒之后，又会和其他线程一起公平竞争对象的锁
    * 在某一个时刻，只有一个线程可以拥有对象的锁
    
> synchronized

* 修饰代码块	
    * 使用synchronized修饰代码块时字节码层面上是通过monitorenter和monitorexit指令来实现锁的获取与释放动作
    * 当线程进入到monitorenter指令后，线程会持有Monitor对象，退出monitorenter指令后，线程会释放Monitor对象
* 修饰方法
    * 对于synchronized关键字修饰方法来说，并没有出现monitorenter和monitorexit指令，而是出现了一个ACC_SYNCHRONIZED标志
    * JVM使用了ACC_SYNCHRONIZED访问标志区分一个方法是否为同步方法：当方法被调用时，调用指令会检查该方法是否拥有ACC_SYNCHRONIZED标志，如果有，那么执行线程将会先持有该方法所在对象的Monitor对象，然后方法执行期间，其他任何线程都无法再获取到这个Monitor，当线程执行完这个方法后，它会释放掉这个Monitor对象
* JVM中的同步是基于进入与退出监视器对象(管程对象)(Monitor)来实现的，每个对象实例都会有一个Monitor对象，Monitor对象会和Java对象一同创建并销毁，Monitor对象是由C++来实现的
* 当多个线程同时访问一段同步代码时，这些线程会被放到一个EntryList集合中，处于阻塞状态的线程都会被放到这个列表当中。接下来，当线程获取到对象的Monitor时，Monitor是依赖于底层操作系统的mutex lock来实现互斥的，线程获取mutex成功，则会持有该mutex，这时其他线程就无法再获取到该mutex
* 如果线程调用了wait方法，那么该线程就会释放掉所持有的mutex，并且该线程会进入到waitSet等待集合中，等待下一次被其他线程调用notify/notifyAll唤醒。如果当前线程顺利执行完毕方法，那么它也会释放掉所持有的mutex
* 同步锁在这种实现方式当中，因为Monitor是依赖底层的操作系统的实现，这样就存在用户态和内核态之间的切换，所以会增加性能开销
* 通过对象互斥锁的概念来保证共享数据操作的完整性，每个对象都对应一个可成为互斥锁的标记，这个标记用于保证在任何时刻，只能有一个线程访问该对象
* 那些处于EntryList(阻塞队列)与WaitSet(等待集合)中的线程均处于阻塞状态，阻塞操作是由操作系统来完成的，在linux下是通过pthread_mutex_lock函数来实现的。线程被阻塞之后便会进入到内核调度状态，这会导致系统在用户态和内核态之间来回切换，严重影响锁的性能
* 解决上述问题的办法是自旋(Spin)。其原理是：当发生对Monitor的竞争时，如果Owner能够在很短的时间内释放掉锁，则那些正在争用的线程就可以稍微等待一下，所谓的自旋，在Owner线程释放掉锁之后，争用线程可能会立即获取到锁，从而避免了系统阻塞。不过，当Owner运行的时间超过了临界值后，争用线程自旋一段时间之后依然无法获取到锁，这时争用线程就会停止自旋而进入到阻塞状态。所以总体的思想是：先自旋，不成功再进行阻塞，尽量降低阻塞的可能性，这对那些执行时间很短的代码块来说有很大的性能提升。显然，自旋在多处理器(多核心)上才有意义
* 互斥锁的属性
    * PTHREAD_MUTEX_TIMED_NP：这是缺省值，也就是普通锁。当一个线程加锁以后，其余请求锁的线程会形成一个等待队列，并且在解锁后按照优先级获取到锁。这种策略可以确保资源分配的公平性
    * PTHREAD_MUTEX_RECURSIVE_NP：嵌套锁。允许一个线程对同一个锁成功获取多次，并通过unlock解锁。如果是不同线程请求，则在加锁线程解锁时重新进行竞争
    * PTHREAD_MUTEX_ERRORCHECK_NP：检错锁，如果一个线程请求同一个锁，则返回EDEADLK，否则与PTHREAD_MUTEX_TIMED_NP类型动作相同，这样就保证了当不允许多次加锁时不会出现最简单情况下的死锁
    * PTHREAD_MUTEX_ADAPTIVE_NP：适应锁，动作最简单的锁类型，仅仅等待解锁后重新竞争
    
---

* 在jdk1.5之前，如果想实现线程同步，只能通过synchronized关键字这一种方式实现，在其底层，java也是通过synchronized关键字来做到数据结构的原子性维护的。synchronized关键字是jvm实现的一种内置锁，从底层角度来讲，这种锁的获取与释放都是由jvm帮助进行隐性实现的
* 从jdk1.5开始，并发包引入了Lock锁，Lock同步锁是基于java实现的，因此锁的获取与释放都是通过java代码来实现和控制的。然而synchronized关键字是基于底层操作系统的Mutex Lock来实现的，每次对锁的获取与释放动作都会带来用户态与内核态之间的切换，这种切换会极大增加系统的负担。在并发量比较高的场景下，也就是锁的竞争比较激烈时，synchronized锁的性能上的表现就非常差
* 从jdk1.6开始，synchronized锁的实现发生了很大的变化：jvm引入了相应的手段提升synchronized锁的性能，这种提升涉及到偏向锁、轻量级锁、重量级锁，从而减少锁的竞争所带来的用户态与内核态之间的切换。这种锁的优化实际上通过java对象头中的一些标志位来实现的，对于锁的访问与改变，实际上都与java对象头息息相关
* 从jdk1.6开始，对象实例在堆中会被划分为3个组成部分：对象头、实例数据、对齐填充
    * 对象头由3个部分内容组成
        * Mark Word
        * 指向类的指针
        * 数组长度
    * 其中Mark Word记录了对象、锁以及垃圾回收相关的信息，在64位的jvm中，其长度是64bit，包括了如下组成部分
        * 无锁标记
        * 偏向锁标记
        * 轻量级锁标记
        * 重量级锁标记
        * GC标记
    * 对于synchronized锁来说，锁的升级主要都是通过Mark Word中的锁的标志位与是否是偏向锁标志位达成的，synchronized关键字锁对应的锁都是先从偏向锁开始，随着锁竞争的不断升级，逐步演化成轻量级锁，最后变成了重量级锁
    * 锁的演化来说会经历：无锁->偏向锁->轻量级锁->重量级锁
        * 偏向锁
            * 针对一个线程来说的，主要作用就是优化同一个线程多次获取同一个锁的情况，如果一个synchronized方法被一个线程访问，那么这个方法所在的对象就会在Mark Word中将偏向锁进行标记，同时还会有一个字段存储该线程id。当这个线程再次访问同一个synchronized方法时，它会检查这个对象的Mark Word的偏向锁标记以及是否指向了这个线程id，如果是的话，那么该线程就不需要进入到管程Monitor了，也就不需要从用户态切换到内核态，而是直接进入到该方法体中
            * 如果是另外一个线程访问这个synchronized方法，偏向锁就会被取消掉
        * 轻量级锁
            * 如果第一个线程已经获取到当前对象的锁，这时第二个线程尝试争抢该对象的锁，由于该对象的锁已经被第一个线程持有，因此它是偏向锁，而第二个线程在争抢时，会发现该对象头中的Mark Word已经是偏向锁，但是里面存储的线程id并不是指向自己，那么他会进行CAS，从而获取到锁，这里存在两种情况
                * 获锁成功，那么它会直接将Mark Word中的线程id从第一个线程变成自己，这样该对象依然会保持偏向锁的状态
                * 获取锁失败，表示这时可能会有多个线程同时在尝试争抢该对象的锁，那么这时偏向锁就会进行升级，升级为轻量级锁
        * 自旋锁
            * 如果自旋失败，依然无法获取到锁，那么锁就会进一步升级为重量级锁，这种情况下，无法获取到锁的线程都会进入到Monitor，也就是内核态
            * 自旋最大的一个特点就是避免了从用户态进入到内核态
        * 重量级锁
            * 线程最终从用户态进入到内核态
            
---

* 编译器对于锁的优化措施
    * 锁消除技术
        * JIT编译器(Just In Time编译器)可以在动态编译同步代码时，使用一种叫做逃逸分析的技术，来通过该项技术判断程序中使用的锁对象是否只被一个线程所使用，而没有散布到其他线程，如果情况是这样的话，那么JIT编译器在编译这个同步代码时就不会生成synchronized关键字所标识的申请与释放机器码，从而消除了锁的使用流程
    * 锁粗化
        * JIT编译器在执行动态编译时，若发现前后相等的synchronized块使用的是同一个锁对象，那么它就会把这几个synchronized块结合成一个较大的同步块，这样做的好处在于线程在执行这些代码时，就无需频繁申请与释放锁了，从而达到申请与释放锁一次，就可以执行完全部的同步代码块，从而提升性能

---

* 死锁
    * 线程1等待线程2互斥持有的资源，而线程2也在等待线程1互斥持有的资源，两个线程都无法继续执行
* 活锁
    * 线程持续重试一个总是失败的操作，导致无法继续执行
* 饿死
    * 线程一直被调度器延迟访问其依赖的资源，也许是调度器先优先级的线程而执行高优先级的线程，同时总会有一个高优先级的线程可以执行，饿死也叫做无限延迟
    
---

> Lock和synchronized关键字在锁的处理上的区别

* 锁的获取方式：前者是通过程序代码的方式由开发者手工获取，后者是通过jvm来获取，无需开发者干预
* 具体实现方式：前者是通过java代码的方式来实现的，后者是通过jvm底层来实现，无需开发者关注
* 锁的释放方式：前者务必通过unlock()方法在finally块中手工释放，后者是通过jvm来释放，无需开发者关注
* 锁的具体类型：前者提供了多种，如公平锁、非公平锁，后者与前者均提供了可重入锁

---

> 线程通讯
* 传统上，使用synchronized关键字+wait+notify/notifyAll来实现多个线程之间的协调和通信，整个过程都是由jvm来帮助实现的，开发者无需了解底层实现细节
* jdk1.5开始，并发包提供了Lock+Condition(await+signal/signalAll)来实现多个线程之间的协调和通信，整个过程都是由开发者来控制的，而且相比于传统方式，更加灵活，功能也更加强大
* Thread.sleep与await（或者object的wait方法）的本质区别：sleep方法本质不会释放锁，而await会释放锁，并且在signal之后，还需要重新获取锁才能继续执行(该行为与Object的wait方法完全一致)

---

> volatile

* volatile的作用
  * 实现long/double 8个字节长度类型变量的原子操作(64位的数字写入内存是分两次 高32位写入和低32位写入)
  * 防止指令重排序
  * 实现变量的可见性

* 当使用volatile修饰变量时，应用不会从寄存器中获取该变量，而是从驻内存中获取
* 如果要实现volatile写操作的原子性，那么在等号右侧的赋值变量中就不能出现被多线程所共享的变量，哪怕这个变量也是volatile修饰的也不行

* volatile和锁的比较
  * volatile可以确保对写操作的原子性 但不具备排他性
  * 使用锁可能会导致线程上下文的切换(内核态和用户态之间) 但使用volatile不会出现这种情况
  
* 防止指令重排序与实现变量的可见性都是通过内存屏障实现的 memory barrier
```text
volatile写入

int a =1;
String s = "dingrui";

内存屏障 Release Barrier: 释放屏障-防止下面的volatile与上面的所有操作指令重排序
volatile boolean b =true;
内存屏障 Store Barrier: 存储屏障-刷新处理器缓存 结果就是确保该存储屏障之前的一切操作生成的结果对其他处理器可见
```

```text
volatile读取

内存屏障 Load Barrier: 加载屏障-可以刷新处理器缓存 同步其他处理器对该volatile变量的修改结果
boolean c = b;
内存屏障 Acquire Barrier: 获取屏障-可以防止上面的volatile读取操作与下面所有操作语句指令重排序
```

* 对于volatile关键字修饰的变量的读写操作 本质都是通过内存屏障来执行的
* 内存屏障兼具了2个能力
  * 防止指令重排序
  * 实现变量内存的可见性
* 对于读取操作 volatile可以确保该操作与后续的读写操作不会进行指令重排序
* 对于修改操作 volatile可以确保该操作与上面的读写操作不会进行指令重排序
* 锁同样具备变量内存可见性与防止指令重排序

```text
synchronized锁

monitorenter
内存屏障: Acquire Barrier
...
内存屏障: Release Barrier
monitorexit
```

---

> java内存模型

* Java Memory Model Java内存模型JMM规范定义了3个问题
  * 变量的原子性问题
  * 变量的可见性问题
  * 变量修改的时序性问题

* happen-before规则
  * 顺序执行规则(限定在单个线程上)：该线程的每个动作都happen-before它后面的动作
  * 隐式锁(monitor)规则：unlock happen-before lock 之前的线程对于同步代码块的所有执行结果对于后续获取锁的线程来说都是可见的
  * volatile读写规则：对于一个volatile变量的写操作一定会happen-before后续对该变量的读操作
  * 多线程的启动规则：Thread对象的start方法happen-before该线程run方法的任何一个动作 包括在其中启动的任何子线程
  * 多线程的终止规则：一个线程启动了一个子线程 并且调用了子线程的join方法等待其结束 那么当线程结束后 父线程的接下来的所有操作都可以看到子线程run方法中的执行结果
  * 线程的中断规则：可以待哦用interrupt方法中断线程 这个调用happen-before对该线程中断的检查isInterrupted
  
---

> CyclicBarrier

* CyclicBarrier执行流程
  * 初始化CyclicBarrier的时候对各种成员变量进行赋值，包括parties、count以及Runnable
  * 当调用await方法时，底层会先检查计数器是否已经归零，如果已经归零，那么就先执行可选的Runnable的run方法，接下来开始下一个generation
  * 在下一个分代中，将会重置count值为parties，并且创建新的Generation实例
  * 同时调用Condition的signalAll方法，唤醒所有在屏障前面等待的线程，让其开始继续执行
  * 如果计数器没有归零的话，当前的调用线程会通过Condition的await方法在屏障钱等待
  * 以上所有执行流程都在lock锁的控制范围内，不会出现并发情况

---

> CAS

* synchronized关键字和Lock锁机制都是悲观锁：无论做何种操作，首先都要先上锁，接下来再去执行后续的操作，从而确保接下来的所有的操作都是由当前这个线程在执行的
* 乐观锁：线程在操作之前不会被做任何的预先处理，而是直接去执行，当最后执行变量更新的时候，当前线程需要有一种机制去确保当前操作的变量没有被其他线程修改，cas是乐观锁的一种极为重要的实现方式
* cas：compare and swap 比较与交换 这是一个不断循环的过程，一直到变量值被修改成功为止，cas本身是由硬件指令提供支持的，硬件是通过一个原子指令来实现比较和交换的，cas可以确保变量操作的原子性
* java中锁的3中实现
  * synchronized
  * AQS
  * StampedLock
  
* 对于CAS来说 操作数主要涉及3个
  * 需要被操作的内存值V
  * 需要进行比较的值A
  * 需要进行写入的值B
  
* CAS的限制和问题
  * 循环开销问题：并发量大的时候导致线程一直自旋，cpu开销大
  * 只能保证一个变量的原子操作：可以通过AtomicReference来实现多个变量的原子操作
  * ABA问题 解决方案是版本号和StampedLock
  
---

* ThreadLocal
  * 本质上，ThreadLocal是通过空间来换取时间，从而实现每个线程中都有一个变量的副本，这样每个线程都会操作该副本，而从完全规避了多线程的并发问题

---

### AQS ReentrantLock的执行逻辑

* 尝试获取对象的锁，如果获取不到，意味着已经有其他线程持有了锁，并且尚未释放，那么就进入到AQS的阻塞队列当中
* 如果获取到，那么根据公平锁还是非公平锁进行不同的处理
  * 如果是公平锁，那么线程会直接放置到AQS阻塞队列末尾
  * 如果是非公平锁，那么线程会首先尝试进行CAS计算，如果成功，则姐获取到锁；如果失败，则与公平锁的处理方式一样，被放置到阻塞队列中
* 当锁被释放时，调用unlock方法，那么底层会调用release方法对state成员变量进行减1的操作，如果减1之后state的值不为0，那么release操作执行完毕；如果减1之后state值为0，则调用LockSupport的unpark方法唤醒该线程之后的等待队列中的第一个，将其唤醒，使之能够获取到对象的锁；之所以调用release方法之后state值可能不为0，原因在于ReentrantLock是可重入锁，表示线程可以多次调用lock方法
* 对于ReentrantLock而言，所谓的上锁，本质上就是对AQS中state成员变量的操作，对该成员变量+1表示上锁；对该成员变量-1表示释放锁

---

### AQS ReentrantReadWriteLock

* 读锁
  * 在获取读锁时，会尝试判断当前对象是否拥有了写锁，如果已经拥有写锁，则直接失效
  * 如果当前线程没有写锁，就表示当前的对象没有拍他锁，则当前线程会尝试给对象加锁
  * 如果当前线程已经拥有了该对象的读锁，那么直接将读锁数量+1
* 写锁
  * 在获取写锁时，会尝试判断当前对象是否拥有了锁(读锁或者写锁)，如果已经拥有了锁且持有的线程并非当前线程，直接失效
  * 如果当前对象没有被加锁，那么写锁就会为当前对象上锁，并且将锁的数量+1
  * 将当前对象的拍他锁线程持有者设置为自己

---

### AQS vs synchronized

* synchronized
  * synchronized关键字在底层的cpp实现中，存在两个重要的数据结构：waitSet、entryList
  * waitSet中存放的是调用了Object对象的wait方法的线程对象(被封装成了cpp的Node对象)
  * entryList中存放的是陷入阻塞状态、需要获取monitor的那些线程对象
  * 当一个线程被notify后，他就会从waitSet移动到entryList中
  * 进入到entryList后，该线程依然需要与其他线程进行竞争monitor锁
  * 如果争抢到锁，就表示该线程获取到了对象的锁，它就以排他方式执行相应的同步代码
  
* AQS
  * AQS中存在两种队列，分别是Condition对象上的条件队列，以及AQS本身的阻塞队列
  * 这两个队列中的每一个对象都是Node示例(Node里面封装了线程对象)
  * 当位于Condition条件队列中的线程被其他线程signal后，该线程就会从条件队列移动到AQS的阻塞队列中
  * 位于AQS阻塞队列中的Node对象本质上都是由一个双向链表构成的
  * 在获取AQS锁时，这些进入阻塞队列中的线程会按照在队列中的排序先后尝试获取
  * 当AQS阻塞队列中的线程获取到锁后，就表示该线程已经可以正常执行了
  * 陷入到阻塞状态的线程，依然需要进入到操作系统的内核态，进入阻塞(park方法实现)

---

### 线程池

* 状态迁移
  * RUNNING->SHUTDOWN 当调用了线程池的shutdown方法时，或者当finalize方法被隐式调用后(该方法内部会调用shutdown方法)
  * RUNNING/SHUTDOWN->STOP 当调用了线程池的shutdownNow方法后
  * SHUTDOWN->TIDYING 在线程池与阻塞队列均变空的时候
  * TIDYING->TERMINATED 在terminated方法被执行完毕时
  
