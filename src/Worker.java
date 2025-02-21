package src;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Worker Class - IMPORTANT
 * The worker class uses Mutex to each have their own thread
 * <p>
 * Important functions:
 * <p>
 * run - the process implementation for oranges that a worker must do
 */
public class Worker implements Runnable {
    // A BlockingQueue (allows for mutex)
    private final BlockingQueue<Orange> queue;
    // A plant that the worker is working at
    private final Plant plant;
    // A worker's name
    private final String worker;

    /**
     * Constructor to create a worker
     * <p>
     * Use a BlockingQueue for mutex
     * Refer to article: https://www.baeldung.com/java-blocking-queue
     *
     * @param queue  - BlockingQueue - mutex implementation for workers/oranges
     * @param plant  - Plant - the plant where the worker is working
     * @param worker - Worker - the person working
     */
    public Worker(BlockingQueue<Orange> queue, Plant plant, String worker) {
        this.queue = queue;
        this.plant = plant;
        this.worker = worker;
    }

    /**
     * The run function that allows for workers to do their work
     */
    @Override
    public void run() {
        try {
            while (true) {
                Orange orange = queue.poll(1000, TimeUnit.MILLISECONDS);
                if (orange == null) {
                    // Exit only when queue is empty AND plant is stopping
                    if (!plant.isTimeToWork() && queue.isEmpty()) {
                        break;
                    }
                    // Keep checking for new oranges
                    continue;
                }

                // Check the state of the orange and print its status
                while (orange.getState() != Orange.State.Bottled) {
                    orange.runProcess();
                    System.out.println(worker + " processed orange to state: " + orange.getState());
                }

                // Yay, an orange got processed!
                plant.incrementProcessed();
            }
        } catch (InterruptedException ignore) {
            System.out.println(worker + " stopping");
            Thread.currentThread().interrupt(); // Is this redundant? Probably. Am I keeping it? Yes, just in case.
        }
    }
}
