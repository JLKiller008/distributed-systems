package Dateien.ueb01;

public class Threading {
    private static final SyncedLogger logger = new SyncedLogger();
    private static final int INCREMENTS_PER_THREAD = 100_000;

    public static void main(String[] args) throws InterruptedException {
        logger.log("Starting threading test");

        Counter counter = new Counter();

        Thread firstIncrementer = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                counter.increment();
            }
        });

        Thread secondIncrementer = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                counter.increment();
            }
        });

        firstIncrementer.start();
        secondIncrementer.start();

        firstIncrementer.join();
        secondIncrementer.join();

        logger.log(String.format("Unsynchronized counter: expected %d, actual %d",
            INCREMENTS_PER_THREAD * 2, counter.value()));

        // Synced Counter

        logger.log("--------------------------------------------------");

        SyncedCounter syncedCounter = new SyncedCounter();

        Thread firstSyncedIncrementer = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                syncedCounter.increment();
            }
        });

        Thread secondSyncedIncrementer = new Thread(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                syncedCounter.increment();
            }
        });

        firstSyncedIncrementer.start();
        secondSyncedIncrementer.start();

        firstSyncedIncrementer.join();
        secondSyncedIncrementer.join();

        logger.log(String.format("Synchronized counter: expected %d, actual %d",
            INCREMENTS_PER_THREAD * 2, syncedCounter.value()));
    }
}
