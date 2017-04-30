package com.officelife.planning.ops.wildling;

import com.officelife.actors.Person;
import com.officelife.goals.State;
import com.officelife.planning.StateScore;

public class WildlingStateScore implements StateScore {

    private State state;

    public WildlingStateScore(State state) {
        this.state = state;
    }
    public WildlingStateScore(){}

    @Override
    public WildlingStateScore make(State state) {
        return new WildlingStateScore(state);
    }

    @Override
    public long score() {
        return (state.actor instanceof Person) ?
                ((Person)state.actor).energy + ((Person)state.actor).belonging :
                0;
    }
}
