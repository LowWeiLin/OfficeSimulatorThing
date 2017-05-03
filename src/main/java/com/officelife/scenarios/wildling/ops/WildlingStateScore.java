package com.officelife.scenarios.wildling.ops;

import com.google.common.collect.Sets;
import com.officelife.scenarios.Person;
import com.officelife.core.WorldState;
import com.officelife.core.planning.Fact;
import com.officelife.core.planning.StateScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class WildlingStateScore implements StateScore {
    private static final Logger logger = LoggerFactory.getLogger(WildlingStateScore.class);
    private WorldState worldState;

    public WildlingStateScore(WorldState worldState) {
        this.worldState = worldState;
    }
    public WildlingStateScore(){}

    @Override
    public WildlingStateScore make(WorldState worldState) {
        return new WildlingStateScore(worldState);
    }

    @Override
    public long score() {
        return (worldState.actor instanceof Person) ?
                ((Person) worldState.actor).energy + ((Person) worldState.actor).belonging :
                0;
    }

    @Override
    public Set<Fact> facts() {
        if (!(worldState.actor instanceof Person)) {
            return Collections.emptySet();
        }

//        if (((Person) worldState.actor).energy < 50) {
//            return Sets.newHashSet(new Fact("I am starving"));
//        } else {
//            Set<Fact> result = Sets.newHashSet(new Fact("i am not that hungry anymore"));
//            if (((Person) worldState.actor).belonging > 50) {
//                result.add(new Fact("I feel safe now"));
//            }
            return facts();
//        }

    }
}
