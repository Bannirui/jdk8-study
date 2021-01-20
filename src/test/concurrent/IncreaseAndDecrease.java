package test.concurrent;

/**
 *@author dingrui
 *@date 2021-01-20
 *@description
 */
public class IncreaseAndDecrease {

    private int counter;

    public synchronized void increase() {
        if (counter != 0) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        counter++;
        System.out.println(counter);
        notify();

    }

    public synchronized void decrease() {
        if (counter == 0) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        counter--;
        System.out.println(counter);
        notify();
    }
}
