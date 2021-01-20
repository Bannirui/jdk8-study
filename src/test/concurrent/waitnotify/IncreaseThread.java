package test.concurrent.waitnotify;

/**
 *@author dingrui
 *@date 2021-01-20
 *@description
 */
public class IncreaseThread extends Thread {

    private IncreaseAndDecrease increaseAndDecrease;

    public IncreaseThread(IncreaseAndDecrease increaseAndDecrease) {
        this.increaseAndDecrease = increaseAndDecrease;
    }

    @Override
    public void run() {
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            increaseAndDecrease.increase();
        }
    }
}
