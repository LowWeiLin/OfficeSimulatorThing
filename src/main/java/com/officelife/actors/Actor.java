package com.officelife.actors;

import com.officelife.World;
import com.officelife.actions.Action;

public interface Actor {
    String id();

    Action act(World state);

    void changeNeed(ActorNeed need, int value);
}
