package com.officelife.actors;

import com.officelife.World;
import com.officelife.actions.Action;
import com.officelife.items.Item;
import com.officelife.ui.Renderable;

import java.util.List;

public interface Actor extends Renderable {

    String id();

    default Action act(World state) {
        return act(state, true);
    }

    Action act(World state, boolean succeeded);

    List<Item> inventory();

    void addItem(Item item);

    boolean isDead();
}
