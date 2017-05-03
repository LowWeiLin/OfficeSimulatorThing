package com.officelife.scenarios.items;


import com.officelife.core.Actor;

public interface Consumable extends Item {
    void consumedBy(Actor actor);
}
