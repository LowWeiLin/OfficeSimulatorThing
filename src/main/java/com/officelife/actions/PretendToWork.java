package com.officelife.actions;

import com.officelife.World;
import com.officelife.actors.Actor;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.FakeWork;
import com.officelife.locations.Cubicle;

public class PretendToWork implements Action {

    private Actor actor;
    private Cubicle cubicle;

    public PretendToWork(Actor actor, Cubicle cubicle) {
        this.actor = actor;
        this.cubicle = cubicle;
    }

    @Override
    public void accept(World world) {
        Commodity fakeWork = new FakeWork();
        fakeWork.applyToPerson(actor);

        System.err.println("Pretend to work");
    }
}
