package com.officelife.planning;


import com.officelife.goals.State;

import java.util.Set;

public interface StateScore {
    StateScore make(State state);

    long score();

    Set<Fact> facts();
}
