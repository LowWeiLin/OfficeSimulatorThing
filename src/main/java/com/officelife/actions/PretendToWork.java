package com.officelife.actions;

import com.officelife.World;
import com.officelife.actors.Actor;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.FakeWork;
import com.officelife.locations.LocationTrait;

public class PretendToWork implements Action {

    private Actor actor;

    public PretendToWork(Actor actor, LocationTrait cubicle) {
        this.actor = actor;
    }

    @Override
    public void accept(World world) {
        Commodity fakeWork = new FakeWork();
        fakeWork.applyToPerson(actor);

        System.err.println("Pretend to work");
    }
}
