package com.officelife.actors;

import com.officelife.Renderable;
import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.items.Item;

public interface Actor extends Renderable {
    String id();

    Action act(World state);

    void changeNeed(ActorState need, int value);

    void addItem(Item item);
    void removeItem(Item item);
}
