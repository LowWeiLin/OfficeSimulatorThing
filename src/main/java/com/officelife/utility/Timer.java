package com.officelife.utility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Used to throttle computation, so the simulation doesn't run too quickly.
 * Controls the thread that computation runs on.
 */
public class Timer {

    private static final int PERIOD = 1;
    private static final TimeUnit PERIOD_UNIT = TimeUnit.SECONDS;

    private final Supplier<Boolean> isPaused;
    private final Runnable action;
    private final ScheduledExecutorService executor;

    private final int maxTimes;
    private int timesRun = 0;

    @SuppressWarnings("unused")
    public Timer(Supplier<Boolean> isPaused, Runnable action) {
        this(isPaused, action, 0);
    }

    public Timer(Supplier<Boolean> isPaused, Runnable action, int timesToRun) {
        this.action = action;
        this.maxTimes = timesToRun;
        this.isPaused = isPaused;
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::fire, 0, PERIOD, PERIOD_UNIT);
    }

    private void fire() {
        if (isPaused.get()) {
            return;
        }
        timesRun++;
        if (maxTimes > 0 && timesRun > maxTimes) {
            executor.shutdown();
            return;
        }
        action.run();
    }
}
