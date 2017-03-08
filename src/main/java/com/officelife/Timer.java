package com.officelife;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.officelife.ui.GUI;

/**
 * Used to throttle computation, so the simulation doesn't run too quickly.
 * Controls the thread that computation runs on.
 */
class Timer {

    private static final int PERIOD = 1;
    private static final TimeUnit PERIOD_UNIT = TimeUnit.SECONDS;

    private final Runnable action;
    private final ScheduledExecutorService executor;

    private final int maxTimes;
    private int timesRun = 0;

    @SuppressWarnings("unused")
    Timer(Runnable action) {
        this(action, 0);
    }

    Timer(Runnable action, int timesToRun) {
        this.action = action;
        this.maxTimes = timesToRun;
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::fire, 0, PERIOD, PERIOD_UNIT);
    }

    private void fire() {
        timesRun++;
        if (maxTimes > 0 && timesRun > maxTimes) {
            executor.shutdown();
            return;
        }
        action.run();
    }
}
