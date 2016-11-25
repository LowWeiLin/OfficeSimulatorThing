package com.officelife.actions;

import com.officelife.actors.Actor;
import com.officelife.common.Pair;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;
import com.officelife.World;

/**
 * Representation of an user acquiring coffee out of a coffee machine.
 */
public class UseCoffeeMachine implements Action {

    private Actor user;
    private CoffeeMachine coffeeMachine;

    public UseCoffeeMachine(Actor user, CoffeeMachine coffeeMachine) {
        this.user = user;
        this.coffeeMachine = coffeeMachine;
    }

    @Override
    public void accept(World world) {
        Coffee newCoffee = new Coffee();
        user.addItem(newCoffee);

        world.items.put(coffeeMachine.id(), coffeeMachine);

        Pair<Integer, Integer> currentLocation;
        try {
            currentLocation = world.actorLocation(user.id());
        } catch (Exception e) {
            throw new RuntimeException("Unable to find actor in world", e);
        }
        world.locationItems.put(currentLocation, coffeeMachine.id());

        System.err.println("Use coffee machine");
    }
}
