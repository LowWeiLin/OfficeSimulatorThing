package com.officelife.actions;

import com.officelife.goals.State;

public abstract class Action {

    public final State state;

    public Action(State state) {
        this.state = state;
    }

    /**
     * In the current architecture, this should mutate the world directly.
     * Returns true for success, false for failure.
     */
    public abstract boolean accept();
}
