package src;

import javax.lang.model.type.ExecutableType;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Plant Class - Manages workers and multiple plants
 * Modified from Professor Nathan Williams' Multi-Plant Class
 * <p>
 * Important Methods:
 * <p>
 * startPlant - starts up the plant (similar to unlocking the door and allowing work to start)
 * stopPlant - stops the plant (similar to halting work for the day)
 * waitToStop - process is running still but needs to stop soon
 * run - the process of running a plant
 * incrementProcess - the process was run, so increment it
 * getProvidedOranges - track the number of oranges used
 * getProcessedOranges - track the number of oranges processed
 * getBottles - get the number of bottles created
 * getWaste - get the number of wasted (leftover) oranges
 * main - actually runs all the plants and workers
 * isTimeToWork - checks (boolean) whether it is time to conduct orange processing or not
 */
public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 10 * 1000;
    // How many workers do we want to have at each plant
    private static final int NUM_WORKERS = 2;
    // How many plants should we run
    private static final int NUM_PLANTS = 2;
    // How many oranges are needed per bottle
    public final int ORANGES_PER_BOTTLE = 3;

    // Use a LinkedBlockingQueue for mutex
    // Refer to article: https://www.baeldung.com/java-blocking-queue
    private final BlockingQueue<Orange> queue = new LinkedBlockingQueue<>();
    private final ExecutorService workerPool;

    // Obviously need threads
    private final Thread thread;
    // Keep track of the number of oranges provided
    private int orangesProvided;
    // Keep track of the number of oranges processed
    private int orangesProcessed;
    // Keep track of the time to work (true/false)
    private volatile boolean timeToWork;

    // Lock (another piece of the mutex)
    private final Lock lock = new ReentrantLock();


    /**
     * Constructor
     *
     * @param threadNum int - number of threads needed
     */
    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        thread = new Thread(this, "Plant[" + threadNum + "]");
        workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
    }

    /**
     * Starts up the plant
     * <p>
     * Example: similar to unlocking the door and allowing work to start
     */
    public void startPlant() {
        timeToWork = true;
        thread.start();

        // Start each worker in the worker pool
        for (int i = 0; i < NUM_WORKERS; i++) {
            workerPool.execute(new Worker(queue, this, "Worker-" + (i + 1) + "-Plant" + thread.getName()));
        }
    }

    /**
     * Stops the plant
     * <p>
     * Example: similar to halting work for the day
     */
    public void stopPlant() {
        timeToWork = false;

        // Wait for empty queue before shutting down workers
        while (!queue.isEmpty()) {
            try {
                // Let workers finish processing
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        workerPool.shutdown();
        try {
            // If the worker is done, wait, then terminate
            if (!workerPool.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                workerPool.shutdownNow();
            }
            // Ignore interrupted exception and shutdown the worker pool
        } catch (InterruptedException ignore) {
            workerPool.shutdownNow();
        }
    }

    /**
     * Process is running still but needs to stop soon
     */
    public void waitToStop() {
        try {
            thread.join();
            // Ignore the interrupted exception and print that there was a malfunction
        } catch (InterruptedException ignore) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * The process of running a plant and managing new oranges
     * Works with the queue to add processed oranges to it and continue processing
     */
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

    /**
     * The process was run, so increment it. Ensure to lock the threads during incrementation.
     */
    public void incrementProcessed() {
        // Lock the threads!
        lock.lock();
        try {
            orangesProcessed++;
        } finally {
            // Release the threads!
            lock.unlock();
        }
    }

    /**
     * Get the number of oranges provided to the plant
     *
     * @return int - oranges provided to plant
     */
    public int getProvidedOranges() {
        return orangesProvided;
    }

    /**
     * Get the number of oranges processed
     *
     * @return int - oranges processed
     */
    public int getProcessedOranges() {
        return orangesProcessed;
    }

    /**
     * Return the number of bottles processed
     *
     * @return int - bottles processed
     */
    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    /**
     * Return the wasted oranges
     *
     * @return int - wasted oranges
     */
    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }

    /**
     * Main function to operate the threading program and process oranges.
     */
    public static void main(String[] args) {
        ExecutorService plantPool = Executors.newFixedThreadPool(2);

        // Startup the plants; DON'T FORGET TO START THEM SILLY
        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i + 1);
            plants[i].startPlant();
        }

        // Execute the plants (different from START)
        for (Plant p : plants) {
            plantPool.execute(p);
        }

        try {
            Thread.sleep(Plant.PROCESSING_TIME);
            // Print that the main (controller) thread was interrupted
        } catch (InterruptedException ignore) {
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

    /**
     * Checks whether it is time to conduct orange processing or not
     *
     * @return boolean - true/false on whether it's time to work
     */
    public boolean isTimeToWork() {
        return timeToWork;
    }
}