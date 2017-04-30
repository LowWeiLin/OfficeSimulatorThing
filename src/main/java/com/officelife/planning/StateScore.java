package com.officelife.planning;


import com.officelife.World;
import com.officelife.actors.Person;
import com.officelife.goals.State;

public interface StateScore {
    StateScore make(State state);

    long score();
}
