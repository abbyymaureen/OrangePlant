package src;

public class SyncDemo extends Thread {
    private static final int NUM_OBJECTS = 5;

    private static final Object lock = new Object();
    private static int count = 0;

    public static void main(String[] args) {
        Thread[] threads = new Thread[NUM_OBJECTS];
        for (int i = 0; i < NUM_OBJECTS; i++) {
            threads[i] = new SyncDemo(i);
        }
        for (Thread t : threads) {
            try {
                 t.join();
            } catch (InterruptedException ignored) { }
        }
        System.out.println("count=" + count);
    }

    SyncDemo(int num) {
        super("Thread[" + num + "]");
        start();
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " PreSync");
        syncMethod();
        System.out.println(Thread.currentThread().getName() + " PostSync");
    }

    private void syncMethod() {
        synchronized (lock) {
            delay();
            for (int i = 0; i < 1000; i++) {
                count++;
            }
        }
    }

    private void delay() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
    }
}
