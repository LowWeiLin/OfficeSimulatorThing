package com.officelife.items;


import com.officelife.actors.Actor;

public interface Consumable extends Item {
    void consumedBy(Actor actor);
}
