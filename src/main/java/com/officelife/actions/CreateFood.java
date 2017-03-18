package com.officelife.actions;

import com.officelife.goals.State;
import com.officelife.items.Food;

/**
 *
 */
public class CreateFood extends Action {

    public CreateFood(State state) {
        super(state);
    }

    @Override
    public boolean accept() {
        createFood();
        return true;
    }

    private void createFood() {
        Food newFood = new Food();
        state.actor.addItem(newFood);
    }

    @Override
    public String toString() {
        return "Create Food ";
    }
}
