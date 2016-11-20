package com.officelife.commodity;


import com.officelife.characteristics.Characteristic;
import com.officelife.actors.Actor;
import com.officelife.actors.ActorState;

import java.util.Set;

public class Coffee implements Commodity {

    @Override
    public void applyToPerson(Actor actor) {
        actor.changeNeed(ActorState.ENERGY, 5);
        actor.changeNeed(ActorState.HUNGER, -5);
    }

    @Override
    public Set<Characteristic> characteristics() {
        return null;
    }
}
