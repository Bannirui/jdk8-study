package test.delayqueue;

import org.junit.Test;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *@author dingrui
 *@date 2021-01-30
 *@description
 */
public class DelayQueueTest {

    @Test
    public void test1() {
        DelayQueue<Delayed> queue = new DelayQueue<>();
    }

    public static void main(String[] args) {
        DelayQueue<Message> queue = new DelayQueue<>();
        long now = System.currentTimeMillis();
        new Thread(() -> {
            while (true) {
                try {
                    Message message = queue.take();
                    System.out.println(message.deadline - now);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        queue.add(new Message(now + 1000));
        queue.add(new Message(now + 2000));
        queue.add(new Message(now + 3000));
        queue.add(new Message(now + 4000));
        queue.add(new Message(now + 5000));
    }
}

class Message implements Delayed {
    long deadline;

    public Message(long deadline) {
        this.deadline = deadline;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return deadline - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        return ((int) (getDelay(TimeUnit.MICROSECONDS) - o.getDelay(TimeUnit.MILLISECONDS)));
    }

    @Override
    public String toString() {
        return "Message{" +
                "deadline=" + deadline +
                '}';
    }
}
