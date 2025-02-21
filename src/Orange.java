package src;

/**
 * Public Class Orange
 * <p>
 * This class creates an orange object and enumerates the amount of time to process said orange.
 * Class provided by Professor Nathan Williams. No changes (just added documentation).
 * <p>
 * Major Functions:
 * <p>
 * getState - gets the stages of processing of the orange
 * runProcess - process the orange (unless the orange has already been processed)
 * doWork - work on the orange
 */
public class Orange {
    /**
     * Enumerate the state of the orange.
     * <p>
     * Orange can be fetched (15 ms to complete)
     * Orange can be peeled (38 ms to complete)
     * Orange can be squeezed (29 ms to complete)
     * Orange can be bottled (17 ms to complete)
     * Orange can be processed (1 ms to complete)
     */
    public enum State {
        Fetched(15),
        Peeled(38),
        Squeezed(29),
        Bottled(17),
        Processed(1);

        private static final int finalIndex = State.values().length - 1;

        // Variable to hold the total time to process the orange
        final int timeToComplete;

        /**
         * Setter function to assign the time to complete a full orange process
         *
         * @param timeToComplete - int : the time to complete a full orange process
         * @return none
         */
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        /**
         * Determine the state of the orange
         *
         * @return State - orange state's next value
         * <p>
         * EXAMPLE: if orange is currently peeled, then it will return squeezed as the next process
         */
        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    private State state;

    // Initializer
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    /**
     * Basic getter for determining state
     *
     * @return State - state of the orange
     */
    public State getState() {
        return state;
    }

    /**
     * Run Processor - processes the oranges
     * Throws exception if the orange has already been process
     * Takes no arguments, returns nothing, tracks orange state internally
     */
    public void runProcess() {
        // Don't attempt to process an already completed orange
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        doWork();
        state = state.getNext();
    }

    /**
     * Do work - mostly just sleeps while the work is commencing
     * Takes no arguments, returns nothing
     */
    private void doWork() {
        // Sleep for the amount of time necessary to do the work
        try {
            Thread.sleep(state.timeToComplete);
            // Ignore the interrupt exception, print that oranges didn't finish processing
        } catch (InterruptedException ignore) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}
