package src;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {
    private final BlockingQueue<Orange> queue;
    private final Plant plant;
    private final String worker;

    public Worker(BlockingQueue<Orange> queue, Plant plant, String worker) {
        this.queue = queue;
        this.plant = plant;
        this.worker = worker;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Orange orange = queue.poll(1000, TimeUnit.MILLISECONDS);
                if (orange == null) {
                    if (!plant.isTimeToWork() && queue.isEmpty()) {
                        break; // Exit only when queue is empty AND plant is stopping
                    }
                    continue; // Keep checking for new oranges
                }

                while (orange.getState() != Orange.State.Bottled) {
                    orange.runProcess();
                    System.out.println(worker + " processed orange to state: " + orange.getState());
                }

                plant.incrementProcessed();
            }
        } catch (InterruptedException e) {
            System.out.println(worker + " stopping");
            Thread.currentThread().interrupt();
        }
    }
}
