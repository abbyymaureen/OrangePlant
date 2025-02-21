package src;

import javax.lang.model.type.ExecutableType;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 10 * 1000;
    private static final int NUM_WORKERS = 2;
    private static final int NUM_PLANTS = 2;
    public final int ORANGES_PER_BOTTLE = 3;

    private final BlockingQueue<Orange> queue = new LinkedBlockingQueue<>();
    private final ExecutorService workerPool;

    private final Thread thread;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;

    private final Lock lock = new ReentrantLock();


    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + threadNum + "]");
        workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
    }

    public void startPlant() {
        timeToWork = true;
        thread.start();

        for (int i = 0; i < NUM_WORKERS; i++) {
            workerPool.execute(new Worker(queue, this, "Worker-" + (i + 1) + "-Plant" + thread.getName()));
        }
    }

    public void stopPlant() {
        timeToWork = false;

        // Wait for the queue to empty before shutting down workers
        while (!queue.isEmpty()) {
            try {
                Thread.sleep(100); // Allow workers to finish processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
        }
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Processing oranges...");
        while (timeToWork) {
            try {
                Orange orange = new Orange();
                queue.put(orange);

                lock.lock();
                try {
                    orangesProvided++;
                } finally {
                    lock.unlock();
                }

                Thread.sleep(50);
                System.out.print(".");
            } catch (InterruptedException e) {
                System.err.println("Error adding orange to queue.");
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public void incrementProcessed() {
        lock.lock();
        try {
            orangesProcessed++;
        } finally {
            lock.unlock();
        }
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }

    public static void main(String[] args) {
        ExecutorService plantPool = Executors.newFixedThreadPool(2);

        // Startup the plants
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i + 1); // Unique names
            plants[i].startPlant();
        }

        for (Plant p : plants) {
            plantPool.execute(p);
        }

        try {
            Thread.sleep(Plant.PROCESSING_TIME);
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted");
        }

        // Stop the plant, and wait for it to shut down
        for (Plant p : plants) {
            p.stopPlant();
        }
        for (Plant p : plants) {
            p.waitToStop();
        }

        // Summarize the results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles +
                ", wasted " + totalWasted + " oranges");

        plantPool.shutdown();
    }

    public boolean isTimeToWork() {
        return timeToWork;
    }
}