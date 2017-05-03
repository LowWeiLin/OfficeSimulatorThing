package com.officelife.core;

import com.officelife.scenarios.items.Item;
import com.officelife.ui.Renderable;

import java.util.List;

/**
 * An actor plans and acts upon the world.
 */
public interface Actor extends Renderable {

    String id();

    void act(FirstWorld state);

    List<Item> inventory();

    void addItem(Item item);

    boolean isDead();
}
