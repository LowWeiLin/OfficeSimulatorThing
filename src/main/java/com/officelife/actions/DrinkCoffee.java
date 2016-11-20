package com.officelife.actions;

import com.officelife.actors.Actor;
import com.officelife.actors.ActorNeed;
import com.officelife.items.CoffeeMachine;
import com.officelife.World;

/**
 * Action representation of an actor sucking the coffee out of a coffee machine.
 * Will cups and the preparation of coffee be required?
 */
public class DrinkCoffee implements Action {

    private final Actor actor;
    private final CoffeeMachine coffee;

    public DrinkCoffee(Actor actor, CoffeeMachine coffeeMachine) {
        this.actor = actor;
        this.coffee = coffeeMachine;
    }

    @Override
    public void accept(World world) {
        actor.changeNeed(ActorNeed.ENERGY, 5);
        actor.changeNeed(ActorNeed.HUNGER, -5);
    }
}
