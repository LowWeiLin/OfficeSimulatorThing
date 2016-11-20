package com.officelife.actions;

import com.officelife.actors.Actor;
import com.officelife.commodity.Coffee;
import com.officelife.commodity.Commodity;
import com.officelife.items.CoffeeMachine;
import com.officelife.World;

/**
 * Representation of an user acquiring coffee out of a coffee machine.
 */
public class UseCoffeeMachine implements Action {

    private final Actor user;
    private final CoffeeMachine coffee;

    public UseCoffeeMachine(Actor user, CoffeeMachine coffeeMachine) {
        this.user = user;
        this.coffee = coffeeMachine;
    }

    @Override
    public void accept(World world) {
        Commodity coffee = new Coffee();
        coffee.applyToPerson(user);
    }
}
