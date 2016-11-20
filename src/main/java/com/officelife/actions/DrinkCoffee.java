package com.officelife.actions;


import com.officelife.World;
import com.officelife.actors.Actor;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.Food;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;

public class DrinkCoffee implements Action {
    private final Coffee coffee;
    private final Actor user;

    public DrinkCoffee(Actor user, Coffee coffee) {
        this.user = user;
        this.coffee = coffee;
    }

    @Override
    public void accept(World world) {
        Commodity food = new Food();
        food.applyToPerson(user);

        user.removeItem(this.coffee);
    }
}
