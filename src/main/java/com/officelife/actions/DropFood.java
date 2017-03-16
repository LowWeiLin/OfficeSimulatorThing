package com.officelife.actions;

import com.officelife.actors.Actor;
import com.officelife.goals.State;

/**
 *
 */
public class DropFood extends Action {


    public DropFood(State state) {
        super(state);
    }

    @Override
    public boolean accept() {
        return false;
    }
}
