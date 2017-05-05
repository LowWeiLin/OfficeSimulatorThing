package com.officelife.core.changes;


import com.officelife.core.Action;
import com.officelife.core.WorldState;

public class Languish implements Action {

    public Languish() {}

    public Action.State actUpon(WorldState state) {
        return State.SUCCESS;
    }
}
