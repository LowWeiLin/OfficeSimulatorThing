package com.officelife.planning.ops;

import com.officelife.goals.State;
import com.officelife.planning.Fact;
import com.officelife.planning.Op;

import java.util.Set;

/**
 * Created by user on 30/3/2017.
 */
public abstract class WeightlessOp implements Op {

    // This gets the facts before transition
    public int weight(State state, Set<Fact> facts) {
        return 1;
    }

}
