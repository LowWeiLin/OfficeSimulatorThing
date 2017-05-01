package com.officelife.planning.ops.wildling;

import com.google.common.collect.Sets;
import com.officelife.actors.Person;
import com.officelife.goals.State;
import com.officelife.planning.Fact;
import com.officelife.planning.StateScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WildlingStateScore implements StateScore {
    private static final Logger logger = LoggerFactory.getLogger(WildlingStateScore.class);
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

    @Override
    public Set<Fact> facts() {
        if (!(state.actor instanceof Person)) {
            return Collections.emptySet();
        }

        if (((Person) state.actor).energy < 50) {
            return Sets.newHashSet(new Fact("I am starving"));
        } else {
            Set<Fact> result = Sets.newHashSet(new Fact("i am not that hungry anymore"));
            if (((Person) state.actor).belonging > 50) {
                result.add(new Fact("I feel safe now"));
            }
            return result;
        }

    }
}
