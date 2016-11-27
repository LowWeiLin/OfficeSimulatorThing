package com.officelife.commodity;


import com.officelife.actors.Actor;
import com.officelife.actors.ActorState;

public class FakeWork extends Work {
    @Override
    public void applyToPerson(Actor actor) {
        actor.changeNeed(ActorState.SANITY, -10);
        actor.changeNeed(ActorState.ENERGY, -2);
        actor.changeNeed(ActorState.SAFETY, 10);
    }
}
