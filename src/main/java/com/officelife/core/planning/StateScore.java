package com.officelife.core.planning;


import com.officelife.core.WorldState;

import java.util.Set;

public interface StateScore {
    StateScore make(WorldState worldState);

    long score();

    Set<Fact> facts();
}
