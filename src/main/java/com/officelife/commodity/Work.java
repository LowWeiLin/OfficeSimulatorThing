package com.officelife.commodity;


import com.officelife.actors.Actor;
import com.officelife.actors.ActorState;
import com.officelife.characteristics.Characteristic;

import java.util.Set;

public class Work implements Commodity {
    @Override
    public void applyToPerson(Actor actor) {
        actor.changeNeed(ActorState.SANITY, -2);
        actor.changeNeed(ActorState.ENERGY, -2);
    }

    @Override
    public Set<Characteristic> characteristics() {
        return null;
    }
}
