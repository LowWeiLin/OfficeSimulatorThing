package com.officelife;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.officelife.ui.GUI;

/**
 * Used to throttle rendering and computation, so the simulation doesn't run too quickly.
 * Controls the thread that computation runs on.
 *
 * May also be used to batch rendering in future when computation begins to take a long time.
 */
class Timer {

    private static final int PERIOD = 1;
    private static final TimeUnit PERIOD_UNIT = TimeUnit.SECONDS;
    private static final boolean RUN_ON_UI_THREAD = true;

    private final GUI gui;
    private final Runnable action;
    private final ScheduledExecutorService executor;

    private final int maxTimes;
    private int timesRun = 0;

    @SuppressWarnings("unused")
    Timer(GUI gui, Runnable action) {
        this(gui, action, Integer.MAX_VALUE);
    }

    Timer(GUI gui, Runnable action, int timesToRun) {
        this.gui = gui;
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
        if (timesRun > maxTimes) {
            executor.shutdown();
            return;
        }
        if (RUN_ON_UI_THREAD) {
            gui.runAndWait(action);
        } else {
            action.run();
        }
    }
}
